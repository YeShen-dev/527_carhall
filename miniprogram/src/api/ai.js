import request from './request.js'
export function chat(message) { return request.post('/ai/chat', { message }) }
export function getChatHistory() { return request.get('/ai/history') }
export function getRecommendations() { return request.get('/ai/recommend') }
