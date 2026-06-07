import request from './request.js'

export function aiChat(message) {
  return request.post('/ai/chat', { message })
}
