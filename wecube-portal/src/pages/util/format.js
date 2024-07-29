export const formatData = data => {
  let exportData = []
  exportData = data.map(_ => ({
    ..._,
    weTableForm: { ..._ }
  }))

  exportData.forEach(_ => {
    for (const i in _['weTableForm']) {
      if (typeof _['weTableForm'][i] === 'object' && _['weTableForm'][i] !== null) {
        _['weTableForm'][i] = _[i].value || _[i].key_name
      }
    }
  })
  return exportData.map(_ => _.weTableForm)
}
