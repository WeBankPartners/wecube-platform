<template>
  <div class="content-container">
    <BackTop :height="100" :bottom="100" />
    <router-view :key="$route.path" />
  </div>
</template>
<script>
export default {
  mounted() {
    const boxLoading = document.getElementById("boxLoading");
    const boxTitle = document.getElementById("boxTitle");
    boxLoading.style.display = "none";
    boxTitle.style.display = "none";
  }
};
</script>
<style lang="scss">
#app {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100%;
  min-width: 1280px;
}
#nav {
  padding: 30px;
  a {
    font-weight: bold;
    color: #2c3e50;
    &.router-link-exact-active {
      color: #42b983;
    }
  }
}
.ivu-layout,
.ivu-layout-sider {
  height: 100%;
}
.spin-icon-load {
  animation: ani-demo-spin 1s linear infinite;
}
body {
  height: 100%;
  overflow: auto !important;
}
html {
  height: 100%;
}

// error style
.ivu-notice-desc {
  word-break: break-all;
  margin-right: 10px;
  max-height: 200px;
  overflow-y: auto;
}

// form validation style
.validation-form {
  .no-need-validation {
    .ivu-form-item-label:before {
      content: " ";
      display: inline-block;
      margin-right: 4px;
      line-height: 1;
    }
  }
  .ivu-form-item-label:before {
    content: "*";
    display: inline-block;
    margin-right: 4px;
    line-height: 1;
    font-size: 12px;
    color: #ed4014;
  }
}
// to show table x direction scroll bar
.ivu-table-fixed-body {
  height: auto !important;
}
</style>
