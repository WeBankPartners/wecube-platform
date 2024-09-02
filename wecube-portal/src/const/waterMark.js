export function watermark(settings) {
  // 默认设置
  const defaultSettings = {
    watermark_txt: 'text',
    watermark_x: 20, // 水印起始位置x轴坐标
    watermark_y: 20, // 水印起始位置Y轴坐标
    watermark_rows: 20, // 水印行数
    watermark_cols: 20, // 水印列数
    watermark_x_space: 100, // 水印x轴间隔
    watermark_y_space: 50, // 水印y轴间隔
    watermark_color: '#aaa', // 水印字体颜色
    watermark_alpha: 0.4, // 水印透明度
    watermark_fontsize: '15px', // 水印字体大小
    watermark_font: '微软雅黑', // 水印字体
    watermark_width: 230, // 水印宽度
    watermark_height: 80, // 水印长度
    watermark_angle: 15 // 水印倾斜度数
  }
  Object.assign(defaultSettings, settings)
  const oTemp = document.createDocumentFragment()
  // 获取页面最大宽度
  const pWidth = Math.max(document.body.scrollWidth, document.body.clientWidth)
  const cutWidth = pWidth * 0.015
  const pageWidth = pWidth - cutWidth
  // 获取页面最大高度
  // let pageHeight = Math.max(document.body.scrollHeight,document.body.clientHeight)+450;
  const pageHeight = Math.max(document.body.scrollHeight, document.body.clientHeight) - 100
  // let pageHeight = document.body.scrollHeight+document.body.scrollTop;
  // 如果将水印列数设置为0，或水印列数设置过大，超过页面最大宽度，则重新计算水印列数和水印x轴间隔
  if (
    defaultSettings.watermark_cols === 0
    || parseInt(
      defaultSettings.watermark_x
        + defaultSettings.watermark_width * defaultSettings.watermark_cols
        + defaultSettings.watermark_x_space * (defaultSettings.watermark_cols - 1)
    ) > pageWidth
  ) {
    defaultSettings.watermark_cols = parseInt(
      (pageWidth - defaultSettings.watermark_x + defaultSettings.watermark_x_space)
        / (defaultSettings.watermark_width + defaultSettings.watermark_x_space)
    )
    defaultSettings.watermark_x_space = parseInt(
      (pageWidth - defaultSettings.watermark_x - defaultSettings.watermark_width * defaultSettings.watermark_cols)
        / (defaultSettings.watermark_cols - 1)
    )
  }
  // 如果将水印行数设置为0，或水印行数设置过大，超过页面最大长度，则重新计算水印行数和水印y轴间隔
  if (
    defaultSettings.watermark_rows === 0
    || parseInt(
      defaultSettings.watermark_y
        + defaultSettings.watermark_height * defaultSettings.watermark_rows
        + defaultSettings.watermark_y_space * (defaultSettings.watermark_rows - 1)
    ) > pageHeight
  ) {
    defaultSettings.watermark_rows = parseInt(
      (defaultSettings.watermark_y_space + pageHeight - defaultSettings.watermark_y)
        / (defaultSettings.watermark_height + defaultSettings.watermark_y_space)
    )
    defaultSettings.watermark_y_space = parseInt(
      (pageHeight - defaultSettings.watermark_y - defaultSettings.watermark_height * defaultSettings.watermark_rows)
        / (defaultSettings.watermark_rows - 1)
    )
  }
  let x
  let y
  for (let i = 0; i < defaultSettings.watermark_rows; i++) {
    y = defaultSettings.watermark_y + (defaultSettings.watermark_y_space + defaultSettings.watermark_height) * i
    for (let j = 0; j < defaultSettings.watermark_cols; j++) {
      x = defaultSettings.watermark_x + (defaultSettings.watermark_width + defaultSettings.watermark_x_space) * j
      const maskDiv = document.createElement('div')
      maskDiv.id = 'maskDiv' + i + j
      maskDiv.className = 'maskDiv'
      maskDiv.appendChild(document.createTextNode(defaultSettings.watermark_txt))
      // 设置水印div倾斜显示
      maskDiv.style.webkitTransform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      maskDiv.style.MozTransform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      maskDiv.style.msTransform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      maskDiv.style.OTransform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      maskDiv.style.transform = 'rotate(-' + defaultSettings.watermark_angle + 'deg)'
      maskDiv.style.visibility = ''
      maskDiv.style.position = 'absolute'
      maskDiv.style.left = x + 'px'
      maskDiv.style.top = y + 'px'
      maskDiv.style.overflow = 'hidden'
      maskDiv.style.zIndex = '9999'
      maskDiv.style.pointerEvents = 'none' // pointer-events:none 让水印不遮挡页面的点击事件
      maskDiv.style.opacity = defaultSettings.watermark_alpha
      maskDiv.style.fontSize = defaultSettings.watermark_fontsize
      maskDiv.style.fontFamily = defaultSettings.watermark_font
      maskDiv.style.color = defaultSettings.watermark_color
      maskDiv.style.textAlign = 'center'
      maskDiv.style.width = defaultSettings.watermark_width + 'px'
      maskDiv.style.height = defaultSettings.watermark_height + 'px'
      maskDiv.style.display = 'block'
      oTemp.appendChild(maskDiv)
    }
  }
  document.body.appendChild(oTemp)
}
