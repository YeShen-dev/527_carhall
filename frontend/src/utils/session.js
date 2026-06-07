function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16)
  })
}

export function getSessionId() {
  let id = localStorage.getItem('session_id')
  if (!id) {
    id = generateUUID()
    localStorage.setItem('session_id', id)
  }
  return id
}
