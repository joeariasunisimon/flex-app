# Bingo Tracker - Testing & Deployment Checklist

## Pre-Testing Checklist ✓

### Code Quality
- [ ] Code compiles without errors
- [ ] No critical warnings in IDE
- [ ] All files saved and committed
- [ ] No uncommitted changes in git

### Documentation
- [ ] Read IMPLEMENTATION_NOTES.md
- [ ] Reviewed BINGO_ENHANCEMENTS.md
- [ ] Understood architecture from BINGO_ARCHITECTURE.md
- [ ] Familiarized with quick reference

---

## Manual Testing Checklist

### 1. Game Creation
- [ ] Can create a new game with name
- [ ] Game appears in game list after creation
- [ ] Game has correct timestamp
- [ ] Multiple games can be created

### 2. Card Setup (Step-by-Step Flow)
- [ ] Step indicator shows "Step 1 of 5"
- [ ] Progress bar shows 20% on first step
- [ ] Can input numbers in first column (B: 1-15)
- [ ] Invalid numbers show error (red outline)
- [ ] Next button disabled until all 5 numbers in column
- [ ] Next button enabled when valid
- [ ] Can navigate to step 2
- [ ] Previous button works to go back
- [ ] Can complete all 5 steps
- [ ] FREE space is properly marked in center
- [ ] Save Card button appears on step 5
- [ ] Card saves successfully

### 3. Figure Selection
- [ ] Card displays full-screen
- [ ] All 5 columns visible with proper spacing
- [ ] Can tap B column to select
- [ ] B column highlights with proper color
- [ ] Can select I, N, G, O columns
- [ ] Selection info shows "You need to mark 5 cells to win!"
- [ ] Full Card button works
- [ ] Full Card shows 25 cells needed
- [ ] Can change selection
- [ ] Continue button saves selection

### 4. Game Play
- [ ] Game loads with correct name
- [ ] Target figure shows in info card
- [ ] Card displays with current numbers
- [ ] Progress bar visible
- [ ] Progress shows "0 / X cells"
- [ ] Can tap card numbers to mark
- [ ] Marked cells change color
- [ ] Progress updates in real-time
- [ ] Cannot mark same number twice
- [ ] Number selector shows 1-90
- [ ] Can tap numbers in selector
- [ ] Both methods (card and selector) work
- [ ] Progress bar fills as you mark cells
- [ ] Win detection works

### 5. Game Management
- [ ] Game list shows all games
- [ ] Completed games show ✅ badge
- [ ] Restart button clears marks
- [ ] Game still has same card after restart
- [ ] Progress resets to 0 after restart
- [ ] Can play again after restart
- [ ] Delete button shows confirmation dialog
- [ ] Dialog warns cannot be undone
- [ ] Can cancel delete
- [ ] Delete removes game from list
- [ ] Delete removes all marks from database

### 6. Database Verification
- [ ] Games persist after app restart
- [ ] Cards persist after app restart
- [ ] Marks persist during gameplay
- [ ] Restart clears marks from database
- [ ] Delete removes game from database
- [ ] No orphaned cards left after delete

---

## UI/UX Testing Checklist

### Visual Quality
- [ ] Text is readable (16sp for numbers)
- [ ] Colors are distinct and accessible
- [ ] Spacing is consistent
- [ ] Cards have proper elevation
- [ ] Buttons are easily tappable (48dp+)
- [ ] No text overflow or clipping

### Navigation
- [ ] Back button works on all screens
- [ ] Can navigate forward and backward
- [ ] State is preserved during navigation
- [ ] No unexpected crashes

### Performance
- [ ] App is responsive (no lag)
- [ ] Card setup loads quickly
- [ ] Game play is smooth
- [ ] Marking numbers is instant
- [ ] No memory leaks (check with profiler)

### Error Handling
- [ ] Invalid input shows error message
- [ ] Error message disappears when fixed
- [ ] Can recover from errors
- [ ] No crashes on invalid input

---

## Win Condition Testing

### Column Wins (B, I, N, G, O)
- [ ] **B Column Win**: Mark all B column numbers (5 numbers)
  - [ ] Progress shows "5 / 5 cells"
  - [ ] Win detection triggers
  - [ ] Congratulations dialog appears
  
- [ ] **I Column Win**: Mark all I column numbers (5 numbers)
  - [ ] Progress shows "5 / 5 cells"
  - [ ] Win dialog appears
  
- [ ] **N Column Win**: Mark N column + FREE (4 + 1 = 5)
  - [ ] Progress shows "5 / 5 cells"
  - [ ] Win dialog appears
  
- [ ] **G Column Win**: Mark all G column numbers (5 numbers)
  - [ ] Progress shows "5 / 5 cells"
  - [ ] Win dialog appears
  
- [ ] **O Column Win**: Mark all O column numbers (5 numbers)
  - [ ] Progress shows "5 / 5 cells"
  - [ ] Win dialog appears

### Full Card Win
- [ ] **Full Card**: Mark all 25 cells
  - [ ] Progress shows "25 / 25 cells"
  - [ ] Win dialog appears

### Win Dialog
- [ ] Shows "🎉 BINGO! 🎉"
- [ ] Shows game name
- [ ] Shows target figure
- [ ] Has "Continue" button
- [ ] Has "Back to Menu" button
- [ ] Can dismiss dialog
- [ ] Can see game in completed list after

---

## Edge Cases & Stress Testing

- [ ] Create game with very long name
- [ ] Create many games (10+)
- [ ] Mark numbers in random order
- [ ] Rapidly tap multiple numbers
- [ ] Restart game multiple times
- [ ] Delete and recreate same game
- [ ] Switch between games quickly
- [ ] Test with device rotation
- [ ] Test with low memory
- [ ] Test with slow device

---

## Regression Testing

### Existing Features Still Work
- [ ] Welcome screen functional
- [ ] Tool selection works
- [ ] Game list loads correctly
- [ ] Navigation between screens smooth
- [ ] No previous features broken

### Database Integrity
- [ ] No orphaned records
- [ ] No data corruption
- [ ] Proper cascade deletes
- [ ] Proper relationships maintained

---

## Accessibility Testing

- [ ] Text is large enough (minimum 12sp)
- [ ] Colors have sufficient contrast
- [ ] Buttons are large enough (minimum 48dp)
- [ ] Touch targets properly spaced
- [ ] No flashing/seizure-inducing effects
- [ ] Color not sole means of conveying info

---

## Performance Benchmarks

| Operation | Target | Actual |
|-----------|--------|--------|
| App launch | < 2s | ___ |
| Load games | < 500ms | ___ |
| Create game | < 1s | ___ |
| Mark number | < 100ms | ___ |
| Save card | < 500ms | ___ |
| Delete game | < 500ms | ___ |
| Restart game | < 200ms | ___ |

---

## Browser/Device Testing (if applicable)

- [ ] Pixel 5 (Android 12)
- [ ] Pixel 6 (Android 13)
- [ ] Samsung S21 (Android 12)
- [ ] Tablet (Android 12)
- [ ] Foldable device
- [ ] Low-end device (2GB RAM)
- [ ] High-end device (8GB+ RAM)

---

## Sign-Off

### QA Testing
- [ ] **Tester Name**: _______________
- [ ] **Date**: _______________
- [ ] **Result**: ✅ PASSED / ⚠️ ISSUES / ❌ FAILED
- [ ] **Issues Found**: (attach separate issue list)

### Code Review
- [ ] **Reviewer Name**: _______________
- [ ] **Date**: _______________
- [ ] **Result**: ✅ APPROVED / ⚠️ CHANGES NEEDED / ❌ REJECTED
- [ ] **Comments**: (attach separate review notes)

### Deployment Approval
- [ ] **Manager Name**: _______________
- [ ] **Date**: _______________
- [ ] **Result**: ✅ APPROVED / ❌ ON HOLD
- [ ] **Release Notes Ready**: ✅ YES / ❌ NO

---

## Post-Deployment

- [ ] Monitor crash reports
- [ ] Monitor user feedback
- [ ] Check performance metrics
- [ ] Prepare for next release
- [ ] Document any issues for next sprint

---

**Last Updated**: April 13, 2026
**Checklist Version**: 1.0
**Status**: Ready for Testing

