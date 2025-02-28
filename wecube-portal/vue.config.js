/* eslint-disable */
const CompressionPlugin = require('compression-webpack-plugin')

/* eslint-disable */
module.exports = {
  devServer: {
    open: true,
    port: 3000,
    proxy: {
      '/': {
        target: process.env.BASE_URL
      }
    }
  },
  runtimeCompiler: true,
  publicPath: '/',
  productionSourceMap: false,
  chainWebpack: config => {
    // remove the old loader
    const img = config.module.rule('images')
    img.uses.clear()
    // add the new one
    img.use('file-loader').loader('file-loader').options({
      outputPath: 'img'
    })
  },
  configureWebpack: config => {
    // config.optimization = {
    //   runtimeChunk: 'single',
    //   splitChunks: {
    //     chunks: 'all',
    //     minSize: 200000, // 允许新拆出 chunk 的最小体积
    //     maxSize: 500000, // 设置chunk的最大体积为500KB
    //     automaticNameDelimiter: '-',
    //     cacheGroups: {
    //       defaultVendors: {
    //         test: /[\\/]node_modules[\\/]/,
    //         priority: -10
    //       },
    //       default: {
    //         minChunks: 2,
    //         priority: -20,
    //         reuseExistingChunk: true
    //       }
    //     }
    //   }
    // }
    if (process.env.NODE_ENV === 'production') {
      return {
        plugins: [
          new CompressionPlugin({
            algorithm: 'gzip',
            test: /\.js$|\.html$|.\css/, // 匹配文件名
            threshold: 10240, // 对超过10k的数据压缩
            deleteOriginalAssets: false // 不删除源文件
          })
        ]
      }
    }
  }
}
