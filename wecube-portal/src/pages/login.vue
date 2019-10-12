<template>
  <div>
    <div class="body"></div>
    <div class="header-login">
      <div></div>
    </div>
    <br />
    <div class="login-form">
      <input
        type="text"
        placeholder="username"
        v-model="username"
        name="user"
      /><br />
      <input
        type="password"
        placeholder="password"
        v-model="password"
        name="password"
      /><br />
      <input type="button" value="Login" @click="login" />
    </div>
  </div>
</template>
<script>
import { login } from "../api/server";
export default {
  data() {
    return {
      username: "",
      password: ""
    };
  },
  methods: {
    async login() {
      const payload = {
        username: this.username,
        password: this.password
      };
      const { status, message, data } = await login(payload);
      if (status === "OK") {
        let session = window.sessionStorage;
        session.setItem("token", JSON.stringify(data));
        this.$router.push("/homepage");
      }
    }
  }
};
</script>
<style scoped>
.body {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: url("../assets/bg.jpg");
  background-size: cover;
  -webkit-filter: blur(3px);
  z-index: 0;
}

.header-login {
  position: absolute;
  top: calc(50% - 35px);
  left: calc(50% - 355px);
  z-index: 2;
}

.header-login div {
  width: 600px;
  height: 50px;
  background-image: url("../assets/wecube-logo.png");
  background-size: contain;
  background-repeat: no-repeat;
}

.login-form {
  position: absolute;
  top: calc(50% - 75px);
  left: calc(50% - 50px);
  height: 150px;
  width: 350px;
  padding: 10px;
  z-index: 2;
}

.login-form input[type="text"],
input[type="password"],
input[type="button"] {
  width: 250px;
  height: 35px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 2px;
  color: #fff;
  font-size: 13px;
  font-weight: 400;
  padding: 4px;
  margin-top: 10px;
}

.login-form input[type="button"] {
  background: #fff;
  border: 1px solid #fff;
  cursor: pointer;
  border-radius: 2px;
  color: #a18d6c;
  font-size: 16px;
}

.login-form input[type="button"]:hover {
  opacity: 0.8;
}

.login-form input[type="button"]:active {
  opacity: 0.6;
}

.login-form input[type="text"]:focus {
  outline: none;
  border: 1px solid rgba(255, 255, 255, 0.9);
}

.login-form input[type="password"]:focus {
  outline: none;
  border: 1px solid rgba(255, 255, 255, 0.9);
}

.login-form input[type="button"]:focus {
  outline: none;
}

::-webkit-input-placeholder {
  color: rgba(255, 255, 255, 0.6);
}

::-moz-input-placeholder {
  color: rgba(255, 255, 255, 0.6);
}
</style>
