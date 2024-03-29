function setLocalstorage (data) {
  const accessTokenObj = data.find(d => d.tokenType === 'accessToken')
  const refreshTokenObj = data.find(d => d.tokenType === 'refreshToken')
  localStorage.setItem('wecube-accessToken', accessTokenObj.token)
  localStorage.setItem('wecube-accessTokenExpirationTime', refreshTokenObj.expiration)
  localStorage.setItem('wecube-refreshToken', refreshTokenObj.token)
  localStorage.setItem('wecube-refreshTokenExpirationTime', refreshTokenObj.expiration)
}

function clearLocalstorage () {
  localStorage.removeItem('wecube-accessToken')
  localStorage.removeItem('wecube-accessTokenExpirationTime')
  localStorage.removeItem('wecube-refreshToken')
  localStorage.removeItem('wecube-refreshTokenExpirationTime')
}

export { setLocalstorage, clearLocalstorage }
