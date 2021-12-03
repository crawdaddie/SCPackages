TimelineItems {
  var <timeline, <sortedTimestamps;

  *new { arg items;
    ^super.new.init(items)
  }
  init { arg items;
    timeline = ();
    sortedTimestamps = SortedList();
    this.addItem(*items)
  }
  addItem { arg ...items;
    items.do { arg item;
      var newItems = timeline[item.beats] !? { arg existingItems;
        existingItems ++ [item]
        } ?? {
        sortedTimestamps.add(item.beats);
        [item]
      };
      timeline[item.beats] = newItems;
    };
  }
  getRoutineFunc { arg start = 0, loopPoints;
    var timestamps = sortedTimestamps.select { arg timestamp;
      timestamp >= start;
    };

    if (timestamps[0] != start, {
      timestamps = [start] ++ timestamps;
    });

    if (loopPoints.notNil, {
      ^{ arg inval;


      }
    });
 
    ^{ arg inval;
      timestamps.do { arg timestamp, i;
        var nextTimestamp = timestamps[i + 1];
        var delta = nextTimestamp !? { nextTimestamp - timestamp } ?? { nil };
        var events = timeline[timestamp] ?? [];

        delta.postln;
        (
          events: events,
          dur: delta,
          sustain: delta,
          play: {
            ~events.do { arg ev;
              ev.play(storeCtx: ~storeCtx, clock: ~clock);
            }
          }
        ).embedInStream(inval);
      }
    } 
  }
}
