<template>
  <div>
    <h2 style="margin-top: 20px">
      通过定义“六个维度+一个核心”的框架体系，不断迭代构建功能组件，将IT系统领域复杂多样的现实存在抽象建模，进而实现高度一致性的IT系统生命周期（规划设计-生产实现-稳定运维-修改变更-退出下线）的管理。
    </h2>
    <div style="text-align: center; margin-top: 40px;">
      <img :src="imgPath" alt="WeCube" />
    </div>
  </div>
</template>

<script>
const imgPath = require("./images/home.png");
export default {
  data() {
    return {
      imgPath
    };
  }
};
</script>
