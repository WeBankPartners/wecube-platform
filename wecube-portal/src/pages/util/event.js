export const addEvent = (selector, type, fn) => {
  const nodesEl = document.querySelectorAll(selector)
  const length = nodesEl.length
  for (let i = 0; i < length; i++) {
    const node = nodesEl[i]
    node.removeEventListener(type, fn)
    node.addEventListener(type, fn)
  }
}

export const removeEvent = (selector, type, fn) => {
  const nodesEl = document.querySelectorAll(selector)
  const length = nodesEl.length
  for (let i = 0; i < length; i++) {
    const node = nodesEl[i]
    node.removeEventListener(type, fn)
  }
}
