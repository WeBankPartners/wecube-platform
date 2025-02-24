/*
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2025-02-21 11:05:22
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2025-02-24 21:32:38
 * 
 * 备注：采用该脚本生成API权限JSON，需注意以下几点：
 * 1. 页面中用到的API必须在server.js中定义
 * 2. 组件中目前识别通过import { *** } from '@/api/server'(.js可带可不带) 这种形式引入的API，其他形式的导入暂不支持。
 * 3. server.js中定义的API必须以export const开头，并且url不能通过变量方式传递，需要写在server.js中
 *    // export const saveBatchExecute = (url, data) => req.post(url, data)，这种方式传递就不行
 * 4. server.js中api的url需要用模板字符串的形式拼接url
 */

const fs = require('fs');
const path = require('path');
const compiler = require('vue-template-compiler');
const glob = require('glob');

// -------------------------------------------------------------------------------------------------------------------
// -------------------------------------------------生成组件对应api的引用关系--------------------------------------------------------------
// -------------------------------------------------------------------------------------------------------------------

// 根据import语句返回对应API函数列表
function extractApiImports(content) {
  // 匹配 import { *** } from '***server(.js)?' 这种形式的导入语句
  const importRegex = /import\s+\{([^}]+)\}\s+from\s+['"](?:[^'"]*?)server(\.js)?['"]/;
  const match = importRegex.exec(content);
  if (match) {
    const apiList = match[1].trim().split(',').map((item) => item.trim());
    return apiList;
  } else {
    return [];
  }
}

// 提取 custom_api_enum 内容
function extractCustomApiEnum(content) {
  const regex = /custom_api_enum\s*=\s*(\[.*?\])/gs;
  const match = regex.exec(content);
  if (match && match[1]) {
    const apiEnum = eval(match && match[1])
    return apiEnum
  }
  return null;
}


function scanFilesAndExtractImports(rootPath) {
  // 使用 glob 匹配所有 .vue 文件
  const files_vue = glob.sync('**/*.vue', { cwd: rootPath });
  // 使用 glob 匹配所有 .js 文件
  const files_js = glob.sync('**/*.js', { cwd: rootPath });
  // 合并 .vue 和 .js 文件列表
  const files = files_vue.concat(files_js);
  const allImports = []; // 存储每个文件及其导入的 API 列表
  files.forEach((file) => {
    const content = fs.readFileSync(file, 'utf-8');
    const apiImports = extractApiImports(content);
    const customApi = extractCustomApiEnum(content);
    if (apiImports.length > 0) {
      allImports.push({
        path: path.resolve(__dirname, file), // 绝对路径，用于后续定位文件位置
        api: apiImports,
        customApi: customApi
      })
    }
  });

  return allImports;
}

const rootPath_ = path.resolve(__dirname, '');
const allImports = scanFilesAndExtractImports(rootPath_);


// ----------------------------------------------------------------------------------------
// --------------------------------------生成菜单对应组件path的映射关系-----------------
// ----------------------------------------------------------------------------------------

// 菜单对应组件入口集合
const menuPathMap = {
  "COLLABORATION_WORKFLOW_ORCHESTRATION": [
    "src/pages/collaboration/workflow-mgmt",
    "src/pages/collaboration/workflow"
  ],
  "COLLABORATION_PLUGIN_MANAGEMENT": [
    "src/pages/collaboration/plugin-registration-detail",
    "src/pages/collaboration/plugin-registration-list"
  ],
  "ADMIN_SYSTEM_PARAMS": [
    "src/pages/admin/system-params"
  ],
  "ADMIN_RESOURCES_MANAGEMENT": [
    "src/pages/admin/resources/index"
  ],
  "ADMIN_CERTIFICATION": [
    "src/pages/admin/plugin-certification"
  ],
  "ADMIN_USER_ROLE_MANAGEMENT": [
    "src/pages/admin/user-role-management"
  ],
  "ADMIN_SYSTEM_WORKFLOW_REPORT": [
    "src/pages/admin/workflow-report/index"
  ],
  "IMPLEMENTATION_WORKFLOW_EXECUTION": [
    "src/pages/implementation/workflow-execution/index",
    "src/pages/implementation/workflow-execution/normal-execution/template",
    "src/pages/implementation/workflow-execution/execution",
    "src/pages/implementation/workflow-execution/normal-execution/history",
    "src/pages/implementation/workflow-execution/time-execution/create",
    "src/pages/implementation/workflow-execution/time-execution/history",
    "src/pages/implementation/workflow-execution/execution"
  ],
  "IMPLEMENTATION_BATCH_EXECUTION": [
    "src/pages/implementation/batch-execution/index",
    "src/pages/implementation/batch-execution/execution/choose-template.vue",
    "src/pages/implementation/batch-execution/execution/create.vue",
    "src/pages/implementation/batch-execution/execution/list.vue",
    "src/pages/implementation/batch-execution/template/create.vue",
    "src/pages/implementation/batch-execution/template/list.vue"
  ],
  "ADMIN_BASE_MIGRATION": [
    "src/pages/admin/base-migration/index",
    "src/pages/admin/base-migration/export/create.vue",
    "src/pages/admin/base-migration/export/history.vue",
    "src/pages/admin/base-migration/import/create.vue",
    "src/pages/admin/base-migration/import/history.vue"
  ],
  "ADMIN_SYSTEM_DATA_MODEL": [
    "src/pages/admin/system-data-model"
  ]
}
const menuKeysMap = {}
Object.entries(menuPathMap).forEach(([menu, pathArr]) => {
  // 存储同一菜单下的所有组件path
  const allPath = [];
  // 带单下对应的组件入口路径集合
  pathArr.forEach(entry_path => {
    const projectRoot = path.resolve(__dirname, '');
    if (!entry_path.endsWith('.vue')) {
      entry_path = entry_path + '.vue'
    }
    const entryComponentPath = path.resolve(projectRoot, entry_path); // 根据实际情况调整

    // 存储已解析的组件路径，避免重复解析
    const parsedComponents = new Set();

    // 解析组件并提取 key 值
    function parseComponent(filePath) {
        if (parsedComponents.has(filePath)) return;
        parsedComponents.add(filePath);
        let scriptContent = ''
        if (filePath.endsWith('.vue')) {
          const content = fs.readFileSync(filePath, 'utf-8');
          const parsed = compiler.parseComponent(content);
          scriptContent = parsed.script.content;
        } else if (filePath.endsWith('.js')) {
          scriptContent = fs.readFileSync(filePath, 'utf-8');
        }
        allPath.push(filePath);
        
        // 提取 import 语句中的子组件路径
        const importRegex = /import\s+\w+\s+from\s+['"]([^'"]+)['"]/g;
        let importMatch;
        while ((importMatch = importRegex.exec(scriptContent))) {
            const relativePath = importMatch[1].replace(/@/g, path.resolve(__dirname, 'src')); // 替换 @ 别名
            let absolutePath = path.resolve(path.dirname(filePath), relativePath);
            // 如果import引入的文件不是js或者vue文件结尾, 则在后面加上.vue或者.js结尾再尝试查找文件是否存在
            if (!absolutePath.endsWith('.vue') && !absolutePath.endsWith('.js')) {
              absolutePath = absolutePath + '.vue'
              if (!fs.existsSync(absolutePath)) {
                // absolutePath = absolutePath + '.js'
                absolutePath = absolutePath.replace(/\.vue$/, ".js");

              }
            }
            if (fs.existsSync(absolutePath)) {
              parseComponent(absolutePath);
            } else {
              // console.warn(`未找到组件文件: ${absolutePath}`);
            }
        }
    }

    // 从入口组件开始解析
    parseComponent(entryComponentPath);
  })
  menuKeysMap[menu] = allPath
})


// -------------------------------------------------------------------------------------------------------------


// 得到菜单和对应api的集合
const apiMap = {};
allImports.forEach(item => {
  apiMap[item.path] = item.api;
  if (Array.isArray(item.customApi) && item.customApi.length > 0) {
    apiMap[item.path] = [...apiMap[item.path], item.customApi]
    /*数据结构如下
    'D:\\webankCode\\wecube-platform\\wecube-portal\\src\\pages\\admin\\base-migration\\import\\history.vue': [
      'getBaseMigrationImportList',
      'getBaseMigrationImportQuery',
      'updateImportStatus',
       [{
         "url": "/platform/v1/process/definitions/export",
         "method": "post"
       }]
      ]
    */
  }
});

// 遍历第一个JSON对象，将对应的API合并到一个数组中，并去重
for (const [key, values] of Object.entries(menuKeysMap)) {
  menuKeysMap[key] = Array.from(
    values.reduce((acc, uuid) => {
      if (apiMap[uuid]) {
        apiMap[uuid].forEach(api => acc.add(api)); // 使用Set去重
      }
      return acc;
    }, new Set()) // 初始值为一个Set
  );
}
// const jsonString_ = JSON.stringify(menuKeysMap, null, 2);
// fs.writeFileSync(path.join(__dirname, 'node-api-tree.json'), jsonString_, 'utf-8');


// -------------------------------------------------------将api函数名转换成method和url组成的对象----------------------------------------------------


// 读取代码文件
const filePath = path.join(__dirname, 'src/api/server.js');
const code = fs.readFileSync(filePath, 'utf-8');

const apiConfigArr = []
const getApiConfigArr = () => {
  // 将server.js文件切割成api组成的数组结构
  const exportRegex = /export\s+const\s+([\s\S]+?)(?=\nexport\s+const\s+|$)/g;
  const apiArray = [];
  let match;

  while ((match = exportRegex.exec(code)) !== null) {
    const apiDefinition = match[1].trim();
    apiArray.push(apiDefinition);
  }

  apiArray.forEach(apiStr => {
    // 使用正则表达式匹配 key、method 和 url
    const keyMatch = apiStr.match(/(\w+)\s*=/);
    const methodUrlMatch = apiStr.match(/req\.(\w+)\(['"`]([^'"`]+)['"`]/);
    if (keyMatch && methodUrlMatch) {
      const key = keyMatch[1]; // 函数名称
      const method = methodUrlMatch[1].toLowerCase(); // 请求方法名
      const url = methodUrlMatch[2].replace(/\${(.*?)}/g, "${$1}"); // 接口 URL
      apiConfigArr.push({
        key,
        url: url.replace(/\?.*/, ''),
        method
      });
    }
  })
  // 将结果写入 JSON 文件
  // const outputFilePath = path.join(__dirname, 'api_config.json');
  // fs.writeFileSync(outputFilePath, JSON.stringify(apiConfigArr, null, 2));
  // console.log(`API 配置已生成并保存到 ${outputFilePath}`);
}
getApiConfigArr();




// --------------------------------------------------------生成最终结果--------------------------------------------------------


const newMenuKeysMap = {}
Object.keys(menuKeysMap).forEach(key => {
  newMenuKeysMap[key] = []
  menuKeysMap[key].forEach(apiName => {
    apiConfigArr.forEach(item => {
      if (item.key === apiName) {
        newMenuKeysMap[key].push(item)
      }
    })
    if (Array.isArray(apiName) && apiName.length > 0) {
      // 组件内部暴露的自定义api custom_api_enum
      newMenuKeysMap[key].push(...apiName)
    }
  })
})

let finalResult = []
Object.keys(newMenuKeysMap).forEach(key => {
  const menuUrlsObj = {
    menu: key,
    urls: newMenuKeysMap[key]
  }
  finalResult.push(menuUrlsObj)
})

const menuApiMapPath = path.join(__dirname, 'menu-api-map.json');
fs.writeFileSync(menuApiMapPath, JSON.stringify(finalResult, null, 2));
