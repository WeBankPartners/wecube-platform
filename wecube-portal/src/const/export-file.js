export default function exportFile (res) {
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
  } else {
    filename = decodeURI(filename)
  }

  const blob = new Blob([res.data])
  if ('msSaveOrOpenBlob' in navigator) {
    // Microsoft Edge and Microsoft Internet Explorer 10-11
    window.navigator.msSaveOrOpenBlob(blob, filename)
  } else {
    if ('download' in document.createElement('a')) {
      // 非IE下载
      let elink = document.createElement('a')
      elink.download = filename
      elink.style.display = 'none'
      elink.href = window.URL.createObjectURL(blob)
      document.body.appendChild(elink)
      elink.click()
      URL.revokeObjectURL(elink.href) // 释放URL 对象
      document.body.removeChild(elink)
    } else {
      // IE10+下载
      navigator.msSaveOrOpenBlob(blob, filename)
    }
  }
}
