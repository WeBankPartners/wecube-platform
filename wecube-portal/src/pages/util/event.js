export const addEvent = (selector, type, fn) => {
  let nodesEl = document.querySelectorAll(selector);
  let length = nodesEl.length;
  // const func = e => {
  //   fn(e);
  // };
  for (let i = 0; i < length; i++) {
    let node = nodesEl[i];
    node.removeEventListener(type, fn);
    node.addEventListener(type, fn);
  }
};
export const removeEvent = (selector, type, fn) => {
  let nodesEl = document.querySelectorAll(selector);
  let length = nodesEl.length;
  // const func = e => {
  //   fn(e);
  // };
  for (let i = 0; i < length; i++) {
    let node = nodesEl[i];
    node.removeEventListener(type, fn);
  }
};
