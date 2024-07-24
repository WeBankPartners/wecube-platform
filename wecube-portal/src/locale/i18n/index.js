import Vue from 'vue'
import VueI18n from 'vue-i18n'

import zh from 'view-design/dist/locale/zh-CN'
import en from 'view-design/dist/locale/en-US'

import zh_local from './zh-CN.json'
import en_local from './en-US.json'

Vue.use(VueI18n)

const messages = {
  'zh-CN': Object.assign(zh_local, zh),
  'en-US': Object.assign(en_local, en)
}

// Vue.locale('zh-CN', Object.assign(zh, ZH))
// Vue.locale('en-US', Object.assign(en, EN))

export const i18n = new VueI18n({
  locale:
    localStorage.getItem('lang') || (navigator.language || navigator.userLanguage === 'zh-CN' ? 'zh-CN' : 'en-US'),
  messages
})

const navLang = navigator.language

const localLang = navLang === 'zh-CN' || navLang === 'en-US' ? navLang : false

const lang = window.localStorage.getItem('lang') || localLang || 'en-US'
Vue.config.lang = lang

export default {}
