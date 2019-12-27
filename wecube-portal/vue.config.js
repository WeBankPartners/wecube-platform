const CompressionPlugin = require("compression-webpack-plugin");

let isUsingLocalCore = false;

const baseURL = isUsingLocalCore
  ? "http://127.0.0.1:8080"
  : "http://111.230.161.237:49090";

module.exports = {
  devServer: {
    // hot: true,
    // inline: true,
    open: true,
    port: 3000,
    proxy: {
      "/platform": {
        target: baseURL
      },
      "/wecmdb": {
        target: baseURL
      },
      "ui-resources": {
        target: baseURL
      },
      "wecube-monitor": {
        target: "https://sandbox.webank.com"
      },
      "/auth": {
        target: baseURL
      }
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
