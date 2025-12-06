/* navigation helper - ensures feedback button works */
function goToFeedback(){
  try {
    // FIX: Must include the folder name in the static resource path
    window.location.href = '/viewpulse-display/feedback.html';
  } catch(e){
    console.error('goToFeedback error', e);
  }
}