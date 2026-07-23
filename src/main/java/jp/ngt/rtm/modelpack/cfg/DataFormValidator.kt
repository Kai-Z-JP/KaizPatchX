package jp.ngt.rtm.modelpack.cfg

import jp.ngt.rtm.modelpack.state.DataEntry
import jp.ngt.rtm.modelpack.state.DataType
import jp.ngt.rtm.modelpack.state.DataTypeHandlers

data class ValidatedDataFormValue(
    val key: String,
    val entry: DataEntry<*>
)

data class DataFormValidationResult(
    val values: List<ValidatedDataFormValue> = emptyList(),
    val error: String? = null
) {
    val isValid: Boolean
        get() = error == null
}

object DataFormValidator {
    @JvmStatic
    fun validate(
        form: DataFormConfig?,
        input: Map<String, DataEntry<*>>
    ): DataFormValidationResult {
        if (form == null || !form.isValid) {
            return invalid("form definition is not valid")
        }

        val fields = form.getValueFieldList()
        val expectedKeys = fields.map { it.resolvedKey() }.toSet()
        if (input.keys != expectedKeys) {
            return invalid("Field keys do not match the form definition")
        }

        val values = ArrayList<ValidatedDataFormValue>(fields.size)
        for (field in fields) {
            val key = field.resolvedKey()
            val inputValue = input[key] ?: return invalid("Missing field: $key")
            val definition = form.getDefaultValue(key) ?: return invalid("Missing default value: $key")
            val type = DataType.getType(definition.type) ?: return invalid("Unknown field type: $key")
            if (inputValue.type != type) {
                return invalid("Field type does not match the form definition: $key")
            }
            if (type != DataType.LIST && inputValue.toString().length > DataFormConfig.MAX_SCALAR_VALUE_LENGTH) {
                return invalid("Field value is too long: $key")
            }
            val handler = DataTypeHandlers.get(type)
            val error = handler.validateConstraints(definition) ?: handler.validateEntry(inputValue, definition)
            if (error != null) {
                return invalid("$error: $key")
            }
            values += ValidatedDataFormValue(key, inputValue)
        }
        return DataFormValidationResult(values)
    }

    private fun invalid(error: String) = DataFormValidationResult(error = error)
}
