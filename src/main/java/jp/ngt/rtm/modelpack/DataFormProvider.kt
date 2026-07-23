package jp.ngt.rtm.modelpack

import jp.ngt.rtm.modelpack.cfg.DataFormConfig

interface DataFormProvider : IModelSelector {
    val dataFormConfig: DataFormConfig?
    val dataFormPermission: String
}
