export default function exportFile(res) {
  const contentDispositionHeader = res.headers['content-disposition']
  let filename = 'file'
  if (contentDispositionHeader) {
    filename = contentDispositionHeader
      .split(';')
      .find(x => ~x.indexOf('filename'))
      .split('=')[1]
  }
  if (filename === null || filename === undefined || filename === '') {
    filename = 'file'
  }
  else {
    filename = decodeURI(filename)
  }

  const blob = new Blob([res.data])
  if ('msSaveOrOpenBlob' in navigator) {
    // Microsoft Edge and Microsoft Internet Explorer 10-11
    window.navigator.msSaveOrOpenBlob(blob, filename)
  }
  else {
    // 非IE下载
    const elink = document.createElement('a')
    elink.download = filename
    elink.href = window.URL.createObjectURL(blob)
    elink.click()
    window.URL.revokeObjectURL(elink.href) // 释放URL 对象
  }
}
