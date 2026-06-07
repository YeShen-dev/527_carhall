import request from './request.js'
export function getUserInfo() { return request.get('/user/profile') }
export function updateProfile(data) { return request.put('/user/profile', data) }
export function updatePassword(oldPwd, newPwd) { return request.put('/user/password', { oldPassword: oldPwd, newPassword: newPwd }) }
export function getBrowseHistory() { return request.get('/user/history') }
export function addToCollection(productId) { return request.post('/user/collection', { productId }) }
export function removeFromCollection(productId) { return request.delete(`/user/collection/${productId}`) }
export function getCollections() { return request.get('/user/collection') }
