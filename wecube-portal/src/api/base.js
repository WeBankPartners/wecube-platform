import Vue from "vue";
import axios from "axios";
const baseURL = "v1/api/";
const req = axios.create({
  withCredentials: true,
  baseURL,
  timeout: 50000
});

req.defaults.headers.common["Http-Client-Type"] = "Ajax";

const throwError = res => new Error(res.message || "error");

let refreshRequest = null;

req.interceptors.request.use(
  config => {
    return new Promise((resolve, reject) => {
      const currentTime = new Date().getTime();
      let session = window.sessionStorage;
      const token = JSON.parse(session.getItem("token"));
      if (token) {
        const accessToken = token.find(t => t.tokenType === "accessToken");
        const expiration = accessToken.expiration * 1 - currentTime;
        if (expiration < 9 * 60 * 1000 && !refreshRequest) {
          refreshRequest = axios.get("/auth/v1/api/token", {
            headers: {
              Authorization:
                "Bearer " +
                token.find(t => t.tokenType === "refreshToken").token
            }
          });
          refreshRequest.then(
            res => {
              session.setItem("token", JSON.stringify(res.data.data));
              config.headers.Authorization =
                "Bearer " +
                res.data.data.find(t => t.tokenType === "accessToken").token;
              refreshRequest = null;
              resolve(config);
            },
            err => {
              refreshRequest = null;
              window.location.href = window.location.origin + "/" + "#/login";
              session.removeItem("token");
            }
          );
        }
        if (expiration < 9 * 60 * 1000 && refreshRequest) {
          refreshRequest.then(
            res => {
              session.setItem("token", JSON.stringify(res.data.data));
              config.headers.Authorization =
                "Bearer " +
                res.data.data.find(t => t.tokenType === "accessToken").token;
              refreshRequest = null;
              resolve(config);
            },
            err => {
              refreshRequest = null;
              window.location.href = window.location.origin + "/" + "#/login";
              session.removeItem("token");
            }
          );
        }
        if (expiration > 9 * 60 * 1000) {
          config.headers.Authorization = "Bearer " + accessToken.token;
          resolve(config);
        }
      } else {
        resolve(config);
      }
    });
  },
  error => {
    return Promise.reject(error);
  }
);
req.interceptors.response.use(
  res => {
    if (res.status === 200) {
      if (res.data.status === "ERROR") {
        const errorMes = Array.isArray(res.data.data)
          ? res.data.data.map(_ => _.errorMessage).join("<br/>")
          : res.data.message;
        Vue.prototype.$Notice.warning({
          title: "Error",
          desc: errorMes,
          duration: 0
        });
      }
      return {
        ...res.data,
        user: res.headers["current_user"] || " - "
      };
    } else {
      return {
        data: throwError(res)
      };
    }
  },
  res => {
    const { response } = res;
    Vue.prototype.$Notice.warning({
      title: "Error",
      desc:
        (response.data &&
          "status:" +
            response.data.status +
            "<br/> error:" +
            response.data.error +
            "<br/> message:" +
            response.data.message) ||
        "error"
    });
    return new Promise((resolve, reject) => {
      resolve({
        data: throwError(res)
      });
    });
  }
);

function setHeaders(obj) {
  Object.keys(obj).forEach(key => {
    req.defaults.headers.common[key] = obj[key];
  });
}

export default req;

export { setHeaders };
