const { request } = require('./request');

function getExercises(date) {
  return request('/exercises?date=' + encodeURIComponent(date));
}

function createExercise(data) {
  return request('/exercises', 'POST', data);
}

function updateExercise(id, data) {
  return request('/exercises/' + id, 'PUT', data);
}

function deleteExercise(id) {
  return request('/exercises/' + id, 'DELETE');
}

function getExerciseRecommendations(date) {
  return request('/exercise-recommendations?date=' + encodeURIComponent(date));
}

module.exports = { createExercise, deleteExercise, getExerciseRecommendations, getExercises, updateExercise };
