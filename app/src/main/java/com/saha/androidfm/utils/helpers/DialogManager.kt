package com.saha.androidfm.utils.helpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * DialogManager holds the currently requested dialog (if any) as observable state.
 * Your Compose UI can collect [dialog] and render the corresponding dialog Composable.
 */
object DialogManager {

    private val _dialog = MutableStateFlow<DialogSpec?>(null)
    val dialog: StateFlow<DialogSpec?> = _dialog.asStateFlow()

    fun dismiss() {
        _dialog.value = null
    }

    fun show(spec: DialogSpec) {
        _dialog.value = spec.withDismiss(::dismiss)
    }

    fun error(build: ErrorDialogBuilder.() -> Unit) {
        show(ErrorDialogBuilder().apply(build).build())
    }

    fun success(build: SuccessDialogBuilder.() -> Unit) {
        show(SuccessDialogBuilder().apply(build).build())
    }

    fun confirmation(build: ConfirmationDialogBuilder.() -> Unit) {
        show(ConfirmationDialogBuilder().apply(build).build())
    }
}

/* ---------------------------- Dialog Models ---------------------------- */

sealed interface DialogSpec {
    val title: String

    /**
     * If false: back press / outside click should NOT dismiss the dialog.
     * Buttons can still dismiss (because actions are wrapped via withDismiss()).
     */
    val cancellable: Boolean

    fun withDismiss(dismiss: () -> Unit): DialogSpec
}

data class DialogButton(
    val text: String,
    val onClick: () -> Unit
) {
    fun withDismiss(dismiss: () -> Unit): DialogButton =
        copy(onClick = {
            dismiss()
            onClick()
        })
}

data class ErrorDialogSpec(
    override val title: String,
    override val cancellable: Boolean,
    val message: String,
    val action: DialogButton,
    val onDismiss: (() -> Unit)? = null
) : DialogSpec {
    override fun withDismiss(dismiss: () -> Unit): DialogSpec =
        // Don't auto-dismiss on action button click - let the user decide in their callback
        this
}

data class SuccessDialogSpec(
    override val title: String,
    override val cancellable: Boolean,
    val message: String,
    val action: DialogButton,
    val onDismiss: (() -> Unit)? = null
) : DialogSpec {
    override fun withDismiss(dismiss: () -> Unit): DialogSpec =
        // Don't auto-dismiss on action button click - let the user decide in their callback
        this
}

data class ConfirmationDialogSpec(
    override val title: String,
    override val cancellable: Boolean,
    val description: String,
    val positive: DialogButton,
    val negative: DialogButton,
    val onDismiss: (() -> Unit)? = null
) : DialogSpec {
    override fun withDismiss(dismiss: () -> Unit): DialogSpec =
        // Don't auto-dismiss on button click - let the user decide in their callback
        this
}

/* ---------------------------- Builders + Validation ---------------------------- */

class ErrorDialogBuilder {
    private var title: String? = null
    private var message: String? = null
    private var actionText: String? = null
    private var action: (() -> Unit)? = null
    private var cancellable: Boolean = true
    private var onDismiss: (() -> Unit)? = null

    fun title(value: String) = apply { title = value }
    fun message(value: String) = apply { message = value }

    /** Default = true */
    fun cancellable(value: Boolean) = apply { cancellable = value }

    fun action(text: String, onClick: () -> Unit) = apply {
        actionText = text
        action = onClick
    }

    fun onDismiss(callback: () -> Unit) = apply {
        onDismiss = callback
    }

    fun build(): ErrorDialogSpec {
        val t = title.requireNotBlank("ErrorDialog.title")
        val m = message.requireNotBlank("ErrorDialog.message")
        val at = actionText.requireNotBlank("ErrorDialog.actionText")
        val a = action.requireNotNull("ErrorDialog.action")

        return ErrorDialogSpec(
            title = t,
            cancellable = cancellable,
            message = m,
            action = DialogButton(text = at, onClick = a),
            onDismiss = onDismiss
        )
    }
}

class SuccessDialogBuilder {
    private var title: String? = null
    private var message: String? = null
    private var actionText: String? = null
    private var action: (() -> Unit)? = null
    private var cancellable: Boolean = true
    private var onDismiss: (() -> Unit)? = null

    fun title(value: String) = apply { title = value }
    fun message(value: String) = apply { message = value }

    /** Default = true */
    fun cancellable(value: Boolean) = apply { cancellable = value }

    fun action(text: String, onClick: () -> Unit) = apply {
        actionText = text
        action = onClick
    }

    fun onDismiss(callback: () -> Unit) = apply {
        onDismiss = callback
    }

    fun build(): SuccessDialogSpec {
        val t = title.requireNotBlank("SuccessDialog.title")
        val m = message.requireNotBlank("SuccessDialog.message")
        val at = actionText.requireNotBlank("SuccessDialog.actionText")
        val a = action.requireNotNull("SuccessDialog.action")

        return SuccessDialogSpec(
            title = t,
            cancellable = cancellable,
            message = m,
            action = DialogButton(text = at, onClick = a),
            onDismiss = onDismiss
        )
    }
}

class ConfirmationDialogBuilder {
    private var title: String? = null
    private var description: String? = null

    private var positiveText: String? = null
    private var positiveAction: (() -> Unit)? = null

    private var negativeText: String? = null
    private var negativeAction: (() -> Unit)? = null

    private var cancellable: Boolean = true
    private var onDismiss: (() -> Unit)? = null

    fun title(value: String) = apply { title = value }
    fun description(value: String) = apply { description = value }

    /** Default = true */
    fun cancellable(value: Boolean) = apply { cancellable = value }

    fun positive(text: String, onClick: () -> Unit) = apply {
        positiveText = text
        positiveAction = onClick
    }

    fun negative(text: String, onClick: () -> Unit) = apply {
        negativeText = text
        negativeAction = onClick
    }

    fun onDismiss(callback: () -> Unit) = apply {
        onDismiss = callback
    }

    fun build(): ConfirmationDialogSpec {
        val t = title.requireNotBlank("ConfirmationDialog.title")
        val d = description.requireNotBlank("ConfirmationDialog.description")

        val pt = positiveText.requireNotBlank("ConfirmationDialog.positiveText")
        val pa = positiveAction.requireNotNull("ConfirmationDialog.positiveAction")

        val nt = negativeText.requireNotBlank("ConfirmationDialog.negativeText")
        val na = negativeAction.requireNotNull("ConfirmationDialog.negativeAction")

        return ConfirmationDialogSpec(
            title = t,
            cancellable = cancellable,
            description = d,
            positive = DialogButton(text = pt, onClick = pa),
            negative = DialogButton(text = nt, onClick = na),
            onDismiss = onDismiss
        )
    }
}

/* ---------------------------- Validation Helpers ---------------------------- */

private fun String?.requireNotBlank(fieldName: String): String {
    require(!this.isNullOrBlank()) { "$fieldName is required and must not be blank." }
    return this
}

private fun <T> T?.requireNotNull(fieldName: String): T {
    require(this != null) { "$fieldName is required and must not be null." }
    return this
}

/* ---------------------------- Example Usage ---------------------------- */
/*
val dialogManager = DialogManager()

dialogManager.error {
    title("Login failed")
    message("Your session has expired. Please sign in again.")
    action("OK") { /* navigate to login */ }
}

dialogManager.success {
    title("Uploaded")
    message("Your document has been uploaded successfully.")
    action("Great") { /* do something */ }
}

dialogManager.confirmation {
    title("Delete file?")
    description("This action cannot be undone.")
    positive("Delete") { /* delete */ }
    negative("Cancel") { /* no-op */ }
}
*/
