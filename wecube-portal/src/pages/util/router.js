export const getChildRouters = routes => {
  if (window.myMenus) {
    const allLinks = [].concat(...window.myMenus.map(_ => _.submenus))
    allLinks.forEach(_ => {
      const found = routes.find(i => i.path === _.link || i.redirect === _.link)
      if (found && found.children) {
        found.children.forEach(child => {
          const foundchild = window.childRouters.find(r => r.link === `${found.path}/${child.path}`)
          if (!foundchild) {
            window.childRouters.push({
              link: `${found.path}/${child.path}`,
              active: _.active
            })
          }
        })
      }
    })
  }
}
