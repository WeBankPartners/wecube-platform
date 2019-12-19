<template>
  <div>
    <h2 style="margin-top: 20px">
      通过定义“六个维度+一个核心”的框架体系，不断迭代构建功能组件，将IT系统领域复杂多样的现实存在抽象建模，进而实现高度一致性的IT系统生命周期（规划设计-生产实现-稳定运维-修改变更-退出下线）的管理。
    </h2>
    <div style="text-align: center; margin-top: 40px;">
      <img v-if="currentLanguage === 'zh-CN'" :src="cnImgPath" alt="WeCube" />
      <img v-else :src="enImgPath" alt="WeCube" />
    </div>
  </div>
</template>

<script>
const cnImgPath = require("./images/wecube_home.png");
const enImgPath = require("./images/wecube_home_en.png");
export default {
  data() {
    return {
      cnImgPath,
      enImgPath,
      currentLanguage: ""
    };
  },
  async created() {
    this.getLocalLang();
  },
  methods: {
    getLocalLang() {
      this.currentLanguage = localStorage.getItem("lang") || navigator.language;
    }
  }
};
</script>
