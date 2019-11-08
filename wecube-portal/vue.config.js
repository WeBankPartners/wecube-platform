const CompressionPlugin = require("compression-webpack-plugin");

let isUsingLocalCore = true;

const baseURL = isUsingLocalCore
  ? "http://localhost:8080"
  : "http://129.204.99.160:9098";

module.exports = {
  devServer: {
    // hot: true,
    // inline: true,
    open: true,
    port: 3000,
    proxy: {
      "/api": {
        target: baseURL
      },
      "ui-resources": {
        target: "http://129.204.99.160:8888"
      }
      // "/auth": {
      //   target: "http://129.204.99.160:8889"
      // },
      // "/packages": {
      //   target: "http://129.204.99.160:30000"
      // }
    }
  },
  runtimeCompiler: true,
  publicPath: "/",
  chainWebpack: config => {
    // remove the old loader
    const img = config.module.rule("images");
    img.uses.clear();
    // add the new one
    img
      .use("file-loader")
      .loader("file-loader")
      .options({
        outputPath: "img"
      });
  },
  configureWebpack: config => {
    if (process.env.NODE_ENV === "production") {
      return {
        plugins: [
          new CompressionPlugin({
            algorithm: "gzip",
            test: /\.js$|\.html$|.\css/, //匹配文件名
            threshold: 10240, //对超过10k的数据压缩
            deleteOriginalAssets: false //不删除源文件
          })
        ]
      };
    }
  }
};
