// overtaking agent
// using the 5 overtaking phases from [Irzik2009] in a chaining order
// uses plan bundling concept, does not take plan failures (-!) into account.

// initial goal
!init.

// plans

+!init <- !drive.

@fuzzy(0.8)
+!accelerate
  :  current_speed(Speed) &&
     distance_predecessor([Distance|_]) &&
     Distance > Speed
  <-
     Speed++;
     setSpeed( Speed )
  <- fail 
  .

@fuzzy(0.5)
+!decelerate
  :
    current_speed(Speed) &&
    Speed > 10
  <-
    Speed--;
    setSpeed( Speed )
  .

// phase 1: approaching a predecessor
+!approach
  :  distance_predecessor([Distance|_]) && Distance >= 5 <- !approach
  :  distance_predecessor([Distance|_]) && Distance < 5 <- !overtake
  .

// phase 2 to 4: parent plan for the overtaking maneuver
+!overtake
  :  lane(MyLane) && MyLane == right &&       // i'm on right lane
     left(ViewLeft) && ViewLeft == free       // and left lane is free
  <-
     !pull_out;                               // => pull out
     !pass_on

  :  lane(MyLane) && MyLane == right &&       // i'm on right lane
     left(ViewLeft) && ViewLeft == blocked    // and left lane is blocked
  <- !overtake                                // => continue in overtaking mode
  .

// phase 2: pull out
+!pull_out
  :  lane(MyLane) && MyLane == right &&
     left(ViewLeft) && ViewLeft == free
  <-
     changeToLane(left);
     !!accelerate;
     !pass_on
  .

// phase 2: plan recovery
/* -!pull_out
  :  lane(MyLane) && MyLane == right &&
     left(ViewLeft) && ViewLeft == free   // apparently we couldn't change lane, but the left lane seems to be free
  <- !pull_out                            // => retry to pull out

  :  lane(MyLane) && MyLane == left       // lange change succeeded, so

  <- !!accelerate;
     !overtake                            // => return to the overtaking plan
  .

*/

//phase3
+!pass_on
  :  current_speed(CSpeed) && intended_speed(ISpeed) && CSpeed < ISpeed &&
     lane(left) && right(vehicle)
  <- !pass_on

  :  current_speed(CSpeed) && intended_speed(ISpeed) && CSpeed == ISpeed &&
     lane(left) && right(free)
  <- !pull_in

  <- // default
    !!accelerate;
    !pass_on
  .

//phase4
+!pull_in
  :  lane(left) && right(free)
  <-
    changeToLane(right);
    !drive
  .

//phase5 (also phase 0 == free flow driving)
+!drive
  :  distance_predecessor(L) && length(L) == 0  // we don't see anyone in front of us => free flow
     && current_speed(CSpeed) && intended_speed(ISpeed) && CSpeed < ISpeed // we can go faster
  <-
     !!accelerate;
     !drive

  :  distance_predecessor(L) && length(L) > 0 // someone is in front of us -> execute approach plan
  <- !approach
  .