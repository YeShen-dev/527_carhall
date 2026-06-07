export function getUserId() {
  let userId = localStorage.getItem('anonymous_user_id')
  if (!userId) {
    userId = crypto.randomUUID()
    localStorage.setItem('anonymous_user_id', userId)
  }
  return userId
}
