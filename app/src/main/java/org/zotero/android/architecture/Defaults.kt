package org.zotero.android.architecture

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.zotero.android.database.objects.AnnotationsConfig
import org.zotero.android.files.DataMarshaller
import org.zotero.android.architecture.logging.DeviceInfoProvider
import org.zotero.android.pdf.data.LandscapeOrientation
import org.zotero.android.pdf.data.PDFSettings
import org.zotero.android.pdf.data.SavedCropConfiguration
import org.zotero.android.screens.settings.EInkMode
import org.zotero.android.screens.settings.translate.TranslateService
import org.zotero.android.screens.settings.translate.ViwoodsModel
import org.zotero.android.screens.allitems.data.ItemsSortType
import org.zotero.android.screens.citbibexport.data.CitBibExportOutputMethod
import org.zotero.android.screens.citbibexport.data.CitBibExportOutputMode
import org.zotero.android.screens.htmlepub.settings.data.HtmlEpubSettings
import org.zotero.android.screens.itemdetails.data.ItemDetailCreator
import org.zotero.android.sync.LibraryIdentifier
import org.zotero.android.webdav.data.WebDavScheme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class Defaults @Inject constructor(
    private val context: Context,
    private val dataMarshaller: DataMarshaller,
) {
    private val sharedPrefsFile = "ZoteroPrefs"
    private val userId = "userId"
    private val sessionId = "sessionId"
    private val name = "name"
    private val username = "username"
    private val displayName = "displayName"
    private val apiToken = "apiToken"
    private val showSubcollectionItems = "showSubcollectionItems"
    private val lastUsedCreatorNamePresentation = "LastUsedCreatorNamePresentation"
    private val itemsSortType = "ItemsSortType"
    private val showCollectionItemCounts = "showCollectionItemCounts"
    private val performFullSyncGuardKey = "performFullSyncGuardKey"
    private val didPerformFullSyncFix = "didPerformFullSyncFix"
    private val tagPickerShowAutomaticTags = "tagPickerShowAutomaticTags"
    private val tagPickerDisplayAllTags = "tagPickerDisplayAllTags"
    private val isDebugLogEnabled = "isDebugLogEnabled"
    private val eInkMode = "eInkMode"
    private val wasPspdfkitInitialized = "wasPspdfkitInitialized"
    private val pdfSettings = "pdfSettings"
    private val pdfCropConfigurationPrefix = "pdfCropConfiguration"
    private val highlightColorHex = "highlightColorHex"
    private val noteColorHex = "noteColorHex"
    private val squareColorHex = "squareColorHex"
    private val inkColorHex = "inkColorHex"
    private val underlineColorHex = "underlineColorHex"
    private val textColorHex = "textColorHex"
    private val activeLineWidth = "activeLineWidth"
    private val activeEraserSize = "activeEraserSize"
    private val activeFontSize = "activeFontSize"
    private val shareExtensionIncludeTags = "shareExtensionIncludeTags"
    private val quickCopyStyleId = "quickCopyStyleId"
    private val quickCopyCslLocaleId = "quickCopyCslLocaleId"
    private val exportStyleId = "exportStyleId"
    private val exportLocaleId = "exportLocaleId"
    private val quickCopyAsHtml = "quickCopyAsHtml"
    private val htmlEpubSettings = "htmlEpubSettings"
    private val translateService = "translateService"
    private val translateDeepLSecret = "translateDeepLSecret"
    private val translateGeminiSecret = "translateGeminiSecret"
    private val translateGeminiPrompt = "translateGeminiPrompt"
    private val translateViwoodsModel = "translateViwoodsModel"
    private val translateViwoodsPrompt = "translateViwoodsPrompt"

    private val exportOutputMode = "exportOutputMode"
    private val exportOutputMethod = "exportOutputMethod"

    private val lastTimestamp = "lastTimestamp"
    private val lastTranslationCommitHash = "lastTranslationCommitHash"
    private val lastTranslatorCommitHash = "lastTranslatorCommitHash"
    private val lastTranslatorDeleted = "lastTranslatorDeleted"
    private val lastStylesCommitHash = "lastStylesCommitHash"
    private val lastPdfWorkerCommitHash = "lastPdfWorkerCommitHash"
    private val lastCitationProcCommitHash = "lastCitationProcCommitHash"
    private val lastUtilitiesCommitHash = "lastUtilitiesCommitHash"
    private val lastCslLocalesCommitHash = "lastCslLocalesCommitHash"
    private val lastHtmlEpubReaderCommitHash = "lastHtmlEpubReaderCommitHash"

    private val isWebDavEnabled = "isWebDavEnabled"
    private val webDavVerified = "webDavVerified"
    private val webDavUsername = "webDavUsername"
    private val webDavUrl = "webDavUrl"
    private val webDavScheme = "webDavScheme"
    private val webDavPassword = "webDavPassword"

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            sharedPrefsFile,
            Context.MODE_PRIVATE
        )
    }

    fun setHighlightColorHex(str: String) {
        sharedPreferences.edit { putString(highlightColorHex, str) }
    }

    fun getHighlightColorHex(): String {
        return sharedPreferences.getString(highlightColorHex, AnnotationsConfig.defaultActiveColor )!!
    }

    fun setNoteColorHex(str: String) {
        sharedPreferences.edit { putString(noteColorHex, str) }
    }

    fun getNoteColorHex(): String {
        return sharedPreferences.getString(noteColorHex, AnnotationsConfig.defaultActiveColor )!!
    }

    fun setSquareColorHex(str: String) {
        sharedPreferences.edit { putString(squareColorHex, str) }
    }

    fun getSquareColorHex(): String {
        return sharedPreferences.getString(squareColorHex, AnnotationsConfig.defaultActiveColor )!!
    }

    fun setInkColorHex(str: String) {
        sharedPreferences.edit { putString(inkColorHex, str) }
    }

    fun getInkColorHex(): String {
        return sharedPreferences.getString(inkColorHex, AnnotationsConfig.defaultActiveColor )!!
    }

    fun setTextColorHex(str: String) {
        sharedPreferences.edit { putString(textColorHex, str) }
    }

    fun getTextColorHex(): String {
        return sharedPreferences.getString(textColorHex, AnnotationsConfig.defaultActiveColor )!!
    }

    fun setUnderlineColorHex(str: String) {
        sharedPreferences.edit { putString(underlineColorHex, str) }
    }

    fun getUnderlineColorHex(): String {
        return sharedPreferences.getString(underlineColorHex, AnnotationsConfig.defaultActiveColor )!!
    }

    fun setActiveLineWidth(width: Float) {
        sharedPreferences.edit { putFloat(activeLineWidth, width) }
    }

    fun getActiveLineWidth(): Float {
        return sharedPreferences.getFloat(activeLineWidth, 2f )
    }

    fun setActiveEraserSize(width: Float) {
        sharedPreferences.edit { putFloat(activeEraserSize, width) }
    }

    fun getActiveEraserSize(): Float {
        return sharedPreferences.getFloat(activeEraserSize, 10f )
    }

    fun setActiveFontSize(size: Float) {
        sharedPreferences.edit { putFloat(activeFontSize, size) }
    }

    fun getActiveFontSize(): Float {
        return sharedPreferences.getFloat(activeFontSize, 12f )
    }

    fun setUserId(str: Long) {
        sharedPreferences.edit { putLong(userId, str) }
    }

    fun setSessionId(str: String?) {
        sharedPreferences.edit { putString(sessionId, str) }
    }

    fun getSessionId(): String? {
        return sharedPreferences.getString(sessionId, null)
    }

    fun setName(str: String) {
        sharedPreferences.edit { putString(name, str) }
    }

    fun setUsername(str: String) {
        sharedPreferences.edit { putString(username, str) }
    }

    fun getUsername(): String {
        return sharedPreferences.getString(username, "" )!!
    }

    fun setDisplayName(str: String) {
        sharedPreferences.edit { putString(displayName, str) }
    }

    fun getDisplayName(): String {
        return sharedPreferences.getString(displayName, "" )!!
    }

    fun setApiToken(str: String?) {
        sharedPreferences.edit { putString(apiToken, str) }
    }

    fun getApiToken(): String? {
        return sharedPreferences.getString(apiToken, null )
    }

    fun isUserLoggedIn() :Boolean {
        return getApiToken() != null
    }

    fun getUserId(): Long {
        return sharedPreferences.getLong(userId, 0L)
    }

    fun showSubcollectionItems(): Boolean {
        return sharedPreferences.getBoolean(showSubcollectionItems, false)
    }

    fun setShowSubcollectionItems(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(showSubcollectionItems, newValue) }
    }

    fun setCreatorNamePresentation(namePresentation: ItemDetailCreator.NamePresentation) {
        val json = dataMarshaller.marshal(namePresentation)
        sharedPreferences.edit { putString(lastUsedCreatorNamePresentation, json) }
    }

    fun getCreatorNamePresentation(): ItemDetailCreator.NamePresentation {
        val json: String = sharedPreferences.getString(
            lastUsedCreatorNamePresentation,
            null
        )
            ?: return ItemDetailCreator.NamePresentation.separate
        return dataMarshaller.unmarshal(json)
    }

    fun setItemsSortType(sortType: ItemsSortType) {
        val json = dataMarshaller.marshal(sortType)
        sharedPreferences.edit { putString(itemsSortType, json) }
    }

    fun getItemsSortType(): ItemsSortType {
        val json: String = sharedPreferences.getString(
            itemsSortType,
            null
        )
            ?: return ItemsSortType.default
        return dataMarshaller.unmarshal(json)
    }

    fun getPDFSettings(): PDFSettings {
        val json: String = sharedPreferences.getString(
            this.pdfSettings,
            null
        ) ?: return PDFSettings.default()
        val pdfSettings: PDFSettings = try {
            dataMarshaller.unmarshal(json)
        } catch (_: Exception) {
            val migratedJson = json.replace("\"pageFitting\":\"CROP\"", "\"pageFitting\":\"FIT\"")
            val migratedSettings = runCatching<PDFSettings> {
                dataMarshaller.unmarshal(migratedJson)
            }.getOrElse {
                PDFSettings.default()
            }
            setPDFSettings(migratedSettings)
            migratedSettings
        }
        if (pdfSettings.landscapeOrientation == null) {
            pdfSettings.landscapeOrientation = LandscapeOrientation.REVERSE
            setPDFSettings(pdfSettings)
        }
        return pdfSettings
    }

    fun setPDFSettings(
        pdfSettings: PDFSettings,
    ) {
        val json = dataMarshaller.marshal(pdfSettings)
        sharedPreferences.edit { putString(this@Defaults.pdfSettings, json) }
    }

    fun getSavedPdfCropConfiguration(
        attachmentKey: String,
        libraryId: LibraryIdentifier,
    ): SavedCropConfiguration? {
        val json = sharedPreferences.getString(pdfCropConfigurationKey(attachmentKey, libraryId), null)
            ?: return null
        return try {
            dataMarshaller.unmarshal(json)
        } catch (_: Exception) {
            null
        }
    }

    fun setSavedPdfCropConfiguration(
        attachmentKey: String,
        libraryId: LibraryIdentifier,
        configuration: SavedCropConfiguration,
    ) {
        val json = dataMarshaller.marshal(configuration)
        sharedPreferences.edit {
            putString(pdfCropConfigurationKey(attachmentKey, libraryId), json)
        }
    }

    fun clearSavedPdfCropConfiguration(
        attachmentKey: String,
        libraryId: LibraryIdentifier,
    ) {
        sharedPreferences.edit {
            remove(pdfCropConfigurationKey(attachmentKey, libraryId))
        }
    }

    private fun pdfCropConfigurationKey(
        attachmentKey: String,
        libraryId: LibraryIdentifier,
    ): String {
        return "$pdfCropConfigurationPrefix:${libraryId.folderName}:$attachmentKey"
    }

    fun showCollectionItemCounts(): Boolean {
        return sharedPreferences.getBoolean(showCollectionItemCounts, true)
    }

    fun setShowCollectionItemCounts(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(showCollectionItemCounts, newValue) }
    }

    fun didPerformFullSyncFix(): Boolean {
        return sharedPreferences.getBoolean(didPerformFullSyncFix, false)
    }

    fun setDidPerformFullSyncFix(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(didPerformFullSyncFix, newValue) }
    }

    fun isShareExtensionIncludeAttachment(): Boolean {
        return sharedPreferences.getBoolean(shareExtensionIncludeTags, true)
    }

    fun setShareExtensionIncludeAttachment(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(shareExtensionIncludeTags, newValue) }
    }

    fun setTagPickerShowAutomaticTags(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(tagPickerShowAutomaticTags, newValue) }
    }

    fun isTagPickerShowAutomaticTags(): Boolean {
        return sharedPreferences.getBoolean(tagPickerShowAutomaticTags, true)
    }

    fun setTagPickerDisplayAllTags(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(tagPickerDisplayAllTags, newValue) }
    }

    fun isTagPickerDisplayAllTags(): Boolean {
        return sharedPreferences.getBoolean(tagPickerDisplayAllTags, true)
    }

    fun setDebugLogEnabled(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(isDebugLogEnabled, newValue) }
    }

    fun isDebugLogEnabled(): Boolean {
        return sharedPreferences.getBoolean(isDebugLogEnabled, false)
    }

    fun setEInkMode(newValue: EInkMode) {
        sharedPreferences.edit { putString(eInkMode, newValue.name) }
    }

    fun getEInkMode(): EInkMode {
        return if (sharedPreferences.contains(eInkMode)) {
            sharedPreferences.getString(eInkMode, null)
                ?.let { storedValue -> EInkMode.entries.firstOrNull { it.name == storedValue } }
                ?: defaultEInkMode()
        } else {
            defaultEInkMode()
        }
    }

    fun clearEInkModeOverride() {
        sharedPreferences.edit { remove(eInkMode) }
    }

    private fun defaultEInkMode(): EInkMode {
        return if (DeviceInfoProvider.isLikelyEInkDevice) {
            EInkMode.Grayscale
        } else {
            EInkMode.Off
        }
    }

    fun setPspdfkitInitialized(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(wasPspdfkitInitialized, newValue) }
    }

    fun wasPspdfkitInitialized(): Boolean {
        return sharedPreferences.getBoolean(wasPspdfkitInitialized, false)
    }

    fun setLastTimestamp(newValue: Long) {
        sharedPreferences.edit { putLong(lastTimestamp, newValue) }
    }

    fun getLastTimestamp(): Long {
        return sharedPreferences.getLong(lastTimestamp, 0L)
    }

    fun setLastTranslatorCommitHash(newValue: String) {
        sharedPreferences.edit { putString(lastTranslatorCommitHash, newValue) }
    }

    fun getLastTranslatorCommitHash(): String {
        return sharedPreferences.getString(lastTranslatorCommitHash, "") ?: ""
    }

    fun setLastTranslationCommitHash(newValue: String) {
        sharedPreferences.edit { putString(lastTranslationCommitHash, newValue) }
    }

    fun getLastTranslationCommitHash(): String {
        return sharedPreferences.getString(lastTranslationCommitHash, "") ?: ""
    }

    fun getLastTranslatorDeleted(): Long {
        return sharedPreferences.getLong(lastTranslatorDeleted, 0L)
    }

    fun setLastTranslatorDeleted(newValue: Long) {
        sharedPreferences.edit { putLong(lastTranslatorDeleted, newValue) }
    }

    fun setLastStylesCommitHash(newValue: String) {
        sharedPreferences.edit { putString(lastStylesCommitHash, newValue) }
    }

    fun getLastStylesCommitHash(): String {
        return sharedPreferences.getString(lastStylesCommitHash, "") ?: ""
    }

    fun setWebDavEnabled(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(isWebDavEnabled, newValue) }
    }

    fun isWebDavEnabled(): Boolean {
        return sharedPreferences.getBoolean(isWebDavEnabled, false)
    }

    fun setWebDavVerified(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(webDavVerified, newValue) }
    }

    fun isWebDavVerified(): Boolean {
        return sharedPreferences.getBoolean(webDavVerified, false)
    }

    fun setWebDavUsername(str: String?) {
        sharedPreferences.edit { putString(webDavUsername, str) }
    }

    fun getWebDavUsername(): String? {
        return sharedPreferences.getString(webDavUsername, null )
    }

    fun setWebDavPassword(str: String?) {
        sharedPreferences.edit { putString(webDavPassword, str) }
    }

    fun getWebDavPassword(): String? {
        return sharedPreferences.getString(webDavPassword, null )
    }

    fun setWebDavUrl(str: String?) {
        sharedPreferences.edit { putString(webDavUrl, str) }
    }

    fun getWebDavUrl(): String? {
        return sharedPreferences.getString(webDavUrl, null )
    }


    fun setWebDavScheme(scheme: WebDavScheme) {
        val json = dataMarshaller.marshal(scheme)
        sharedPreferences.edit { putString(webDavScheme, json) }
    }

    fun getWebDavScheme(): WebDavScheme {
        val json: String = sharedPreferences.getString(
            webDavScheme,
            null
        )
            ?: return WebDavScheme.https
        return dataMarshaller.unmarshal(json)
    }

    val currentPerformFullSyncGuard = 1

    fun setPerformFullSyncGuard(newValue: Int) {
        sharedPreferences.edit { putInt(performFullSyncGuardKey, newValue) }
    }

    fun performFullSyncGuard(): Int {
        if (!sharedPreferences.contains(performFullSyncGuardKey)) {
            if (sharedPreferences.contains(didPerformFullSyncFix)) {
                return currentPerformFullSyncGuard - 1
            }
            return currentPerformFullSyncGuard
        }
        return sharedPreferences.getInt(performFullSyncGuardKey, 1)
    }

    fun getLastPdfWorkerCommitHash(): String {
        return sharedPreferences.getString(lastPdfWorkerCommitHash, "") ?: ""
    }

    fun setLastPdfWorkerCommitHash(newValue: String) {
        sharedPreferences.edit { putString(lastPdfWorkerCommitHash, newValue) }
    }

    fun getLastCitationProcCommitHash(): String {
        return sharedPreferences.getString(lastCitationProcCommitHash, "") ?: ""
    }

    fun setLastCitationProcCommitHash(newValue: String) {
        sharedPreferences.edit { putString(lastCitationProcCommitHash, newValue) }
    }

    fun getLastUtilitiesCommitHash(): String {
        return sharedPreferences.getString(lastUtilitiesCommitHash, "") ?: ""
    }

    fun setLastUtilitiesCommitHash(newValue: String) {
        sharedPreferences.edit { putString(lastUtilitiesCommitHash, newValue) }
    }

    fun getQuickCopyStyleId(): String {
        return sharedPreferences.getString(quickCopyStyleId, "http://www.zotero.org/styles/chicago-notes-bibliography") ?: ""
    }

    fun setQuickCopyStyleId(newValue: String) {
        sharedPreferences.edit { putString(quickCopyStyleId, newValue) }
    }

    fun getQuickCopyCslLocaleId(): String {
        return sharedPreferences.getString(quickCopyCslLocaleId, "en-US") ?: ""
    }

    fun hasQuickCopyCslLocaleId(): Boolean {
        return sharedPreferences.contains(quickCopyCslLocaleId)
    }

    fun setQuickCopyCslLocaleId(newValue: String) {
        sharedPreferences.edit { putString(quickCopyCslLocaleId, newValue) }
    }

    fun getExportStyleId(): String {
        return sharedPreferences.getString(exportStyleId, "http://www.zotero.org/styles/chicago-notes-bibliography") ?: ""
    }

    fun setExportStyleId(newValue: String) {
        sharedPreferences.edit { putString(exportStyleId, newValue) }
    }

    fun getExportLocaleId(): String {
        return sharedPreferences.getString(exportLocaleId, "en-US") ?: ""
    }

    fun setExportLocaleId(newValue: String) {
        sharedPreferences.edit { putString(exportLocaleId, newValue) }
    }

    fun setQuickCopyAsHtml(newValue: Boolean) {
        sharedPreferences.edit { putBoolean(quickCopyAsHtml, newValue) }
    }

    fun isQuickCopyAsHtml(): Boolean {
        return sharedPreferences.getBoolean(quickCopyAsHtml, false)
    }

    fun getLastCslLocalesCommitHash(): String {
        return sharedPreferences.getString(lastCslLocalesCommitHash, "") ?: ""
    }

    fun setLastCslLocalesCommitHash(newValue: String) {
        sharedPreferences.edit { putString(lastCslLocalesCommitHash, newValue) }
    }

    fun getExportOutputMethod(): CitBibExportOutputMethod {
        val json: String = sharedPreferences.getString(
            this.exportOutputMethod,
            null
        ) ?: return CitBibExportOutputMethod.copy
        return dataMarshaller.unmarshal(json)
    }

    fun setExportOutputMethod(
        method: CitBibExportOutputMethod,
    ) {
        val json = dataMarshaller.marshal(method)
        sharedPreferences.edit { putString(this@Defaults.exportOutputMethod, json) }
    }

    fun getExportOutputMode(): CitBibExportOutputMode {
        val json: String = sharedPreferences.getString(
            this.exportOutputMode,
            null
        ) ?: return CitBibExportOutputMode.bibliography
        return dataMarshaller.unmarshal(json)
    }

    fun setExportOutputMode(
        mode: CitBibExportOutputMode,
    ) {
        val json = dataMarshaller.marshal(mode)
        sharedPreferences.edit { putString(this@Defaults.exportOutputMode, json) }
    }

    fun getLastHtmlEpubReaderCommitHash(): String {
        return sharedPreferences.getString(lastHtmlEpubReaderCommitHash, "") ?: ""
    }

    fun setLastHtmlEpubReaderCommitHash(newValue: String) {
        sharedPreferences.edit { putString(lastHtmlEpubReaderCommitHash, newValue) }
    }

    fun getHtmlEpubSettings(): HtmlEpubSettings {
        val json: String = sharedPreferences.getString(
            this.htmlEpubSettings,
            null
        ) ?: return HtmlEpubSettings.default()
        return dataMarshaller.unmarshal(json)
    }

    fun setHtmlEpubSettings(
        pdfSettings: HtmlEpubSettings,
    ) {
        val json = dataMarshaller.marshal(pdfSettings)
        sharedPreferences.edit { putString(this@Defaults.htmlEpubSettings, json) }
    }

    fun setTranslateService(newValue: TranslateService) {
        sharedPreferences.edit { putString(translateService, newValue.name) }
    }

    fun getTranslateService(): TranslateService {
        return sharedPreferences.getString(translateService, null)
            ?.let { storedValue -> TranslateService.entries.firstOrNull { it.name == storedValue } }
            ?: TranslateService.default()
    }

    fun setTranslateDeepLSecret(newValue: String) {
        sharedPreferences.edit { putString(translateDeepLSecret, newValue) }
    }

    fun getTranslateDeepLSecret(): String {
        return sharedPreferences.getString(translateDeepLSecret, "") ?: ""
    }

    fun setTranslateGeminiSecret(newValue: String) {
        sharedPreferences.edit { putString(translateGeminiSecret, newValue) }
    }

    fun getTranslateGeminiSecret(): String {
        return sharedPreferences.getString(translateGeminiSecret, "") ?: ""
    }

    fun setTranslateGeminiPrompt(newValue: String) {
        sharedPreferences.edit { putString(translateGeminiPrompt, newValue) }
    }

    fun getTranslateGeminiPrompt(): String {
        val storedPrompt = sharedPreferences.getString(translateGeminiPrompt, defaultTranslatePrompt())
            ?: return defaultTranslatePrompt()
        return storedPrompt.ifBlank { defaultTranslatePrompt() }
    }

    fun setTranslateViwoodsModel(newValue: ViwoodsModel) {
        sharedPreferences.edit { putString(translateViwoodsModel, newValue.modelId) }
    }

    fun getTranslateViwoodsModel(): ViwoodsModel {
        return ViwoodsModel.fromModelId(sharedPreferences.getString(translateViwoodsModel, null))
    }

    fun setTranslateViwoodsPrompt(newValue: String) {
        sharedPreferences.edit { putString(translateViwoodsPrompt, newValue) }
    }

    fun getTranslateViwoodsPrompt(): String {
        val storedPrompt = sharedPreferences.getString(translateViwoodsPrompt, defaultTranslatePrompt())
            ?: return defaultTranslatePrompt()
        return storedPrompt.ifBlank { defaultTranslatePrompt() }
    }

    private fun defaultTranslatePrompt(): String {
        return "你有PL相关的专业知识，首先，翻译这个句子为中文，其次在换行拆解句子的语法组成，最后换行用词根解释生僻词（生僻词指CEFR标准中C1范围外的词）。你的输出如，原文:This is an apple.<换行>译文:这是一个苹果<换行>句子结构: [This(主语)] [is(系动词)] [an apple(表语)].<换行>生僻词:N/A 。N/A表示句子里没有生词。 句子是： {text}"
    }

    fun reset() {
        setUsername("")
        setDisplayName("")
        setUserId(0L)
        setSessionId(null)
        setShowSubcollectionItems(false)
        setApiToken(null)
        setItemsSortType(ItemsSortType.default)
        clearEInkModeOverride()

        setActiveLineWidth(1f)
        setInkColorHex(AnnotationsConfig.defaultActiveColor)
        setSquareColorHex(AnnotationsConfig.defaultActiveColor)
        setNoteColorHex(AnnotationsConfig.defaultActiveColor)
        setHighlightColorHex(AnnotationsConfig.defaultActiveColor)
        setUnderlineColorHex(AnnotationsConfig.defaultActiveColor)
        setTextColorHex(AnnotationsConfig.defaultActiveColor)
        setPDFSettings(pdfSettings = PDFSettings.default())

        setWebDavUrl(null)
        setWebDavScheme(WebDavScheme.https)
        setWebDavEnabled(false)
        setWebDavUsername(null)
        setWebDavPassword(null)
        setWebDavVerified(false)

        setQuickCopyCslLocaleId("en-US")
        setQuickCopyAsHtml(false)
        setQuickCopyStyleId("http://www.zotero.org/styles/chicago-notes-bibliography")
        setExportOutputMethod(CitBibExportOutputMethod.copy)
        setExportOutputMode(CitBibExportOutputMode.bibliography)
        setTranslateService(TranslateService.default())
        setTranslateDeepLSecret("")
        setTranslateGeminiSecret("")
        setTranslateGeminiPrompt(defaultTranslatePrompt())
    }

}
