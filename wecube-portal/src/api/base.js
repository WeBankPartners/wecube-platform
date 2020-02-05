import Vue from 'vue'
import axios from 'axios'
const baseURL = ''
const req = axios.create({
  withCredentials: true,
  baseURL,
  timeout: 50000
})

const throwError = res => {
  Vue.prototype.$Notice.warning({
    title: 'Error',
    desc: (res.data && 'status:' + res.data.status + '<br/> message:' + res.data.message) || 'error'
  })
}
let refreshRequest = null

req.interceptors.request.use(
  config => {
    return new Promise((resolve, reject) => {
      const currentTime = new Date().getTime()
      let session = window.sessionStorage
      const token = JSON.parse(session.getItem('token'))
      if (token) {
        const accessToken = token.find(t => t.tokenType === 'accessToken')
        const expiration = accessToken.expiration * 1 - currentTime
        if (expiration < 1 * 60 * 1000 && !refreshRequest) {
          refreshRequest = axios.get('/auth/v1/api/token', {
            headers: {
              Authorization: 'Bearer ' + token.find(t => t.tokenType === 'refreshToken').token
            }
          })
          refreshRequest.then(
            res => {
              session.setItem('token', JSON.stringify(res.data.data))
              config.headers.Authorization = 'Bearer ' + res.data.data.find(t => t.tokenType === 'accessToken').token
              refreshRequest = null
              resolve(config)
            },
            // eslint-disable-next-line handle-callback-err
            err => {
              refreshRequest = null
              window.location.href = window.location.origin + window.location.pathname + '#/login'
              session.removeItem('token')
            }
          )
        }
        if (expiration < 1 * 60 * 1000 && refreshRequest) {
          refreshRequest.then(
            res => {
              session.setItem('token', JSON.stringify(res.data.data))
              config.headers.Authorization = 'Bearer ' + res.data.data.find(t => t.tokenType === 'accessToken').token
              refreshRequest = null
              resolve(config)
            },
            // eslint-disable-next-line handle-callback-err
            err => {
              refreshRequest = null
              window.location.href = window.location.origin + window.location.pathname + '#/login'
              session.removeItem('token')
            }
          )
        }
        if (expiration > 1 * 60 * 1000) {
          config.headers.Authorization = 'Bearer ' + accessToken.token
          resolve(config)
        }
      } else {
        resolve(config)
      }
    })
  },
  error => {
    return Promise.reject(error)
  }
)
req.interceptors.response.use(
  res => {
    if (res.status === 200) {
      if (res.data.status === 'ERROR') {
        const errorMes = Array.isArray(res.data.data)
          ? res.data.data.map(_ => _.errorMessage).join('<br/>')
          : res.data.message
        Vue.prototype.$Notice.warning({
          title: 'Error',
          desc: errorMes,
          duration: 0
        })
      }

      if (res.headers['content-type'] === 'application/octet-stream') {
        let contentDispositionHeader = res.headers['content-disposition']
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
        let url = window.URL.createObjectURL(new Blob([res.data]))
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
        Vue.prototype.$Notice.info({
          title: 'Success',
          desc: '',
          duration: 0
        })

        return
      }
      return res.data instanceof Array ? res.data : { ...res.data }
    } else {
      return {
        data: throwError(res)
      }
    }
  },
  err => {
    const { response } = err
    if (response.status === 401) {
      window.location.href = window.location.origin + window.location.pathname + '#/login'
      // throwInfo(response)
      return response
    }

    return new Promise((resolve, reject) => {
      resolve({
        data: throwError(err)
      })
    })
  }
)

function setHeaders (obj) {
  Object.keys(obj).forEach(key => {
    req.defaults.headers.common[key] = obj[key]
  })
}

export default req

export { setHeaders }
