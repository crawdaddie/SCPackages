(
w=Window().layout_( GridLayout.rows(
  [StaticText().string_("item1").beginDragAction_({ arg view; view.postln }), rows: 1 ],
  [StaticText().string_("item1").beginDragAction_({ arg view; view.postln }), rows: 1 ],
  [StaticText().string_("item1").beginDragAction_({ arg view; view.postln }), rows: 1 ],
  [StaticText().string_("item1").beginDragAction_({ arg view; view.postln }), rows: 1 ],
)).front;
)
(
w = Window.new;
w.layout = VLayout(
    [Button().states_([["Foo"]]), stretch:1, align:\bottomLeft],
    20,
    [TextView().string_("Bar\nBar\nBar\n"), s:3],
    [nil, s:1]
);
w.front;
)
