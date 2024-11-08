function setCookie(tokens) {
  tokens.forEach(_ => {
    document.cookie = `${_.tokenType}=${_.token};path=/`
    document.cookie = `${_.tokenType}ExpirationTime=${_.expiration};path=/`
  })
}

function getCookie(name) {
  // eslint-disable-next-line no-useless-escape
  const reg = new RegExp('(?:(?:^|.*;\\s*)' + name + '\\s*\\=\\s*([^;]*).*$)|^.*$')
  return document.cookie.replace(reg, '$1')
}

function clearCookie() {
  const cookies = document.cookie.split(';')
  // 遍历所有的cookies，并将它们设置为过期
  for (let i = 0; i < cookies.length; i++) {
    const cookie = cookies[i]
    const eqPos = cookie.indexOf('=')
    const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie
    document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/'
  }
}

export { setCookie, getCookie, clearCookie }
