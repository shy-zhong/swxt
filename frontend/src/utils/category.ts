export interface Category {
    id: number
    name: string
    parentId: number
    sortOrder: number
}

export interface TreeNode {
    category: Category
    children: TreeNode[]
}

export function buildTree(list: Category[]): TreeNode[] {
    const map = new Map<number, TreeNode>()
    list.forEach(c => map.set(c.id, { category: c, children: [] }))

    const roots: TreeNode[] = []
    list.forEach(c => {
        const node = map.get(c.id)!
        const parent = c.parentId ? map.get(c.parentId) : undefined
        if (parent) {
            parent.children.push(node)
        } else {
            roots.push(node)
        }
    })

    const sortNodes = (node: TreeNode[]) => {
        node.sort((a, b) => (a.category.sortOrder - b.category.sortOrder) || (a.category.id - b.category.id))
        node.forEach(n => sortNodes(n.children))
    }
    sortNodes(roots)

    return roots
}

export function buildPathMap(list: Category[]): Map<number, string> {
    const byId = new Map<number, Category>()
    list.forEach(c => byId.set(c.id, c))

    const pathMap = new Map<number, string>()
    const resolve = (c: Category, seen: Set<number>): string => {
        if (pathMap.has(c.id)) return pathMap.get(c.id)!

        if (!c.parentId || seen.has(c.id)) return c.name
        const parent = byId.get(c.parentId)
        seen.add(c.id)
        const parentPath = parent ? resolve(parent, seen) : ''
        const path = parentPath ? `${parentPath} / ${c.name}` : c.name
        pathMap.set(c.id, path)
        return path
    }
    list.forEach(c => resolve(c, new Set()))
    return pathMap
}

export function collectDescendantIds(list: Category[], id: number): Set<number> {
    const result = new Set<number>()
    const stack = [id]
    while (stack.length > 0) {
        const current = stack.pop()!
        list.forEach(c => {
            if (c.parentId === current && !result.has(c.id)) {
                result.add(c.id)
                stack.push(c.id)
            }
        })
    }
    return result
}
