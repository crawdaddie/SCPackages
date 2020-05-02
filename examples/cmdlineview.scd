(
// w = Window(bounds: 200@40);
v = [
	"rand",
	"rand2",
	"binomial",
	"gauss",
	"rando",
	"random",
	"qran"
];
q = "";
c = View(bounds: 200@40);
t = TextField();
l = ListView()
	.canFocus_(false);

c.layout_(VLayout(
	t,
	l,
));
c.layout.spacing = 0;
c.layout.margins = 0!4;
	
t.keyDownAction = { arg textView, char, modifiers, unicode, keycode, key;
	[modifiers, key].postln;
	if (q.size != textView.string) {
		var filterList;
		q = textView.string;
		filterList = v.select { |item|
			item.contains(textView.string);
		};
		l.items_(filterList);
		l.value = 0;
	};

	// if ( key == 16777235 ) {
	// 	l.value.postln;
	// 	l.value = (l.value - 1) % l.items.size;
	// };

	// if ( key == 16777237 ) {
	// 	l.value.postln;
	// 	l.value = (l.value + 1) % l.items.size;
	// };
	
	// if ([ modifiers, key ] == [ 1048576, 16777220 ]) {
	// 	q.postln;
		
		// return to parent view
		// c.remove;
		// parent.focus(true);
	// }
};
// w.front;

c.front;

)


"hello".size

(
w = Window.new.front;
w.view.decorator = FlowLayout(w.view.bounds);
g = EZPopUpMenu.new(w, 200@22, "Menu: ");
g.addItem(\item0, { |a| ("this is item 0 of " ++ a).postln });
g.addItem(\item1, { |a| ("this is item 1 of " ++ a).postln });
g.addItem(\item2, { |a| ("this is item 2 of " ++ a).postln });
g.value = 0;
)

(
w = Window(bounds:Rect(200,200,200,200)).layout_(
    VLayout(
        TextField(), TextField(), TextField(),
        HLayout( Button(), Button(), Button() )
    )
).front;
)