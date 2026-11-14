package dev.novux.dynasty.cbb.ui.pack

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.novux.dynasty.cbb.R
import dev.novux.dynasty.cbb.core.pack.PackOverlaySession
import dev.novux.dynasty.cbb.core.pack.import.PackImportAttestation
import dev.novux.dynasty.cbb.core.pack.import.PackImportConstants
import dev.novux.dynasty.cbb.core.pack.import.PackImportFormat
import dev.novux.dynasty.cbb.data.pack.HttpPackRemoteFetcher
import dev.novux.dynasty.cbb.data.pack.PackImportFetchException
import dev.novux.dynasty.cbb.data.pack.PackParseException
import dev.novux.dynasty.cbb.data.pack.PackRepository
import kotlinx.coroutines.launch

private enum class ImportTab { FileOrPaste, Url }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun IdentityPackImportScreen(
    packRepository: PackRepository,
    dynastyMembershipYear: String?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tabIndex by remember { mutableIntStateOf(0) }
    var attestationChecked by remember { mutableStateOf(false) }
    var pasteBody by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activePack by remember { mutableStateOf<PackOverlaySession?>(null) }

    LaunchedEffect(Unit) {
        activePack = packRepository.activeSession()
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            errorMessage = null
            statusMessage = null
            val bytes = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()
            if (bytes == null) {
                busy = false
                errorMessage = context.getString(R.string.pack_import_file_read_failed)
                return@launch
            }
            val attestation = PackImportAttestation(userAcceptedDisclaimer = attestationChecked)
            packRepository.importPackFromBytes(bytes, PackImportFormat.Auto, attestation)
                .onSuccess { swap ->
                    activePack = swap.session
                    statusMessage = context.getString(
                        R.string.pack_import_success,
                        swap.session.packId,
                        swap.session.membershipYear,
                        swap.teamCount,
                    )
                    warnMembershipYear(context, dynastyMembershipYear, swap.session.membershipYear)?.let {
                        statusMessage = "$statusMessage\n$it"
                    }
                }
                .onFailure { err -> errorMessage = formatImportFailure(context, err) }
            busy = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.pack_import_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        activePack?.let { pack ->
            Text(
                text = stringResource(
                    R.string.pack_import_active_pack,
                    pack.packId,
                    pack.membershipYear,
                    pack.leagueDisplayName,
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = PackImportConstants.UNOFFICIAL_DISCLAIMER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = attestationChecked,
                onCheckedChange = { attestationChecked = it },
            )
            Text(
                text = stringResource(R.string.pack_import_attestation_label),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = stringResource(R.string.pack_import_disclaimer_required_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryTabRow(selectedTabIndex = tabIndex) {
            ImportTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = {
                        Text(
                            when (tab) {
                                ImportTab.FileOrPaste -> stringResource(R.string.pack_import_tab_paste)
                                ImportTab.Url -> stringResource(R.string.pack_import_tab_url)
                            },
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        when (ImportTab.entries[tabIndex]) {
            ImportTab.FileOrPaste -> {
                OutlinedButton(
                    onClick = {
                        filePicker.launch(
                            arrayOf(
                                "application/json",
                                "text/plain",
                                "application/*",
                            ),
                        )
                    },
                    enabled = attestationChecked && !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.pack_import_choose_file))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pasteBody,
                    onValueChange = { pasteBody = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pack_import_paste_label)) },
                    minLines = 6,
                    enabled = !busy,
                )
                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            errorMessage = null
                            statusMessage = null
                            val attestation = PackImportAttestation(userAcceptedDisclaimer = attestationChecked)
                            packRepository.importPack(pasteBody, PackImportFormat.Auto, attestation)
                                .onSuccess { swap ->
                                    activePack = swap.session
                                    statusMessage = context.getString(
                                        R.string.pack_import_success,
                                        swap.session.packId,
                                        swap.session.membershipYear,
                                        swap.teamCount,
                                    )
                                    warnMembershipYear(
                                        context,
                                        dynastyMembershipYear,
                                        swap.session.membershipYear,
                                    )?.let { statusMessage = "$statusMessage\n$it" }
                                }
                                .onFailure { errorMessage = formatImportFailure(context, it) }
                            busy = false
                        }
                    },
                    enabled = attestationChecked && pasteBody.isNotBlank() && !busy,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text(stringResource(R.string.pack_import_submit))
                }
            }
            ImportTab.Url -> {
                Text(
                    text = stringResource(R.string.pack_import_url_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text(stringResource(R.string.pack_import_url_label)) },
                    singleLine = true,
                    enabled = !busy,
                )
                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            errorMessage = null
                            statusMessage = null
                            val attestation = PackImportAttestation(userAcceptedDisclaimer = attestationChecked)
                            val fetcher = HttpPackRemoteFetcher()
                            packRepository.importPackFromUrl(url.trim(), attestation, fetcher)
                                .onSuccess { swap ->
                                    activePack = swap.session
                                    statusMessage = context.getString(
                                        R.string.pack_import_success,
                                        swap.session.packId,
                                        swap.session.membershipYear,
                                        swap.teamCount,
                                    )
                                    warnMembershipYear(
                                        context,
                                        dynastyMembershipYear,
                                        swap.session.membershipYear,
                                    )?.let { statusMessage = "$statusMessage\n$it" }
                                }
                                .onFailure { errorMessage = formatImportFailure(context, it) }
                            busy = false
                        }
                    },
                    enabled = attestationChecked && url.isNotBlank() && !busy,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text(stringResource(R.string.pack_import_fetch_url))
                }
            }
        }

        if (busy) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
        errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        statusMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_back))
        }
    }
}

private fun formatImportFailure(context: android.content.Context, error: Throwable): String {
    val detail = when (error) {
        is PackImportFetchException -> error.importError.toString()
        is PackParseException -> error.message
        else -> error.message
    }
    return context.getString(R.string.pack_import_failed, detail ?: error.javaClass.simpleName)
}

private fun warnMembershipYear(
    context: android.content.Context,
    dynastyYear: String?,
    packYear: String,
): String? {
    if (dynastyYear.isNullOrBlank() || dynastyYear == packYear) return null
    return context.getString(R.string.pack_import_membership_year_warning, dynastyYear, packYear)
}
