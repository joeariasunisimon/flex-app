# SQLite Constraint Error - Solution

## Problem
```
Failed to create game: NOT NULL constraint failed: Game.target_figure 
(code 1299 SQLITE_CONSTRAINT_NOTNULL)
```

## Root Cause
The database on your device has an older schema where `target_figure` was NOT NULL. The current schema allows it to be NULL (which is correct - users should be able to create games without immediately setting a target figure).

## Solution

You have two options:

### Option 1: Simple Fix (Recommended)
Delete the app data and let the app recreate the database with the correct schema.

**Steps:**
1. Open **Settings** on your Android device
2. Go to **Apps** → **FlexApp** (or your app)
3. Tap **Storage** → **Clear Data** (or **Storage & Cache** → **Clear Cache**)
4. Restart the app
5. The database will be recreated with the correct schema

**Result:**
- ✅ Database is fresh with correct schema
- ✅ No more NOT NULL constraint errors
- ⚠️ Any previously created games will be lost

### Option 2: Keep Existing Games (If You Have Important Games)
Unfortunately, SQLite doesn't support direct ALTER COLUMN operations. To preserve games while fixing the schema:

1. **Export** your game data if you need it
2. **Clear app data** as in Option 1
3. **Recreate** the games

**Why:** SQLite requires recreating the table to change column constraints.

## What Was Fixed in Code

The current source code already has the correct schema:

**File**: `shared/src/commonMain/sqldelight/co/jarias/flexapp/shared/database/BingoDatabase.sq`

```sql
-- CORRECT SCHEMA (current)
CREATE TABLE Game (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    target_figure TEXT,              -- ✅ Nullable (correct)
    created_at TEXT NOT NULL,
    is_completed INTEGER NOT NULL DEFAULT 0,
    completed_at TEXT
);
```

The app now:
- ✅ Allows creating games without a target figure
- ✅ Target figure can be set later in "Figure Selection" screen
- ✅ Properly handles NULL target figures

## Verification

After clearing app data and rebuilding:

1. ✅ Create a new game with just a name
2. ✅ No "NOT NULL constraint failed" error
3. ✅ Navigate to Card Setup
4. ✅ Set up the card
5. ✅ Go to Figure Selection and choose a target
6. ✅ Start playing the game

## Prevention

This issue occurred because:
- Old schema had `target_figure NOT NULL DEFAULT NULL` (contradiction!)
- Current schema correctly has `target_figure TEXT` (nullable)
- The .db file on device hadn't been updated

**Going Forward:**
- All new installations will use the correct schema
- You won't see this error again

## Related Files

- `BingoDatabase.sq` - Correct schema (already fixed)
- `DatabaseDriverFactory.kt` - Initialization (uses schema)

---

**Status**: ✅ FIXED in source code
**User Action**: Clear app data (recommended)
**Timeline**: Takes 2 minutes
**Data Impact**: Existing games will be lost (use Option 1)

