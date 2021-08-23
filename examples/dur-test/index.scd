// temp code
(
Project.openLatest;
~store = Store.global;
s.boot;
)
'storeEvent'.importFrom('baseEvents')

~store.addObject(~storeEvent.create((beats: 0, row: 2)))
~store.play

~store[1005].play;
~store[1001].play;
