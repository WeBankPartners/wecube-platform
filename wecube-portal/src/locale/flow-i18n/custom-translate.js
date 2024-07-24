import CNtranslations from './cn-translations'

export default function customTranslate(template, replacements) {
  const tempReplacements = replacements || {}

  // Translate
  const TempTemplate = CNtranslations[template] || template

  // Replace
  return TempTemplate.replace(/{([^}]+)}/g, function (_, key) {
    return tempReplacements[key] || '{' + key + '}'
  })
}
