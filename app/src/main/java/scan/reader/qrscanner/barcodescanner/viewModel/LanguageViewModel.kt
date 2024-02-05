package scan.reader.qrscanner.barcodescanner.viewModel

import androidx.lifecycle.ViewModel
import scan.reader.qrscanner.barcodescanner.modelClass.LangModelClass
import scan.reader.qrscanner.barcodescanner.R

class LanguageViewModel:ViewModel() {


    private val languageList= arrayListOf(
        LangModelClass(R.drawable.lang_english_icon,"en","English",true),
        LangModelClass(R.drawable.lang_spain_icon,"es","Español",false),
        LangModelClass(R.drawable.lang_france_icon,"fr","Français",false),
        LangModelClass(R.drawable.lang_german_icon,"de","Deutsch",false),
        LangModelClass(R.drawable.lang_italy_icon,"it","Italiano",false),
        LangModelClass(R.drawable.lang_portugal_icon,"pt","Português",false),
        LangModelClass(R.drawable.lang_korean_icon,"ko","한국인",false)
    )

    fun getLanList()=languageList
}