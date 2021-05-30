+ Store {
	*postTree {
		"global:".post;
		global.postTree(nil, 1);
	}

	postTree { arg obj, tabs = 0;
		if(obj.isNil, { obj = this });
		
		if (obj.class == Store) {
			"".postln;

			obj.keysValuesDo({ arg key, value;
				if (key.class == Symbol) {
					tabs.do({ Char.tab.post });
					
					key.post;
					": ".post;
					value.postln;
				}
			});

			obj.keysValuesDo({ arg key, value;
				if (key.class == Integer) {
					tabs.do({ Char.tab.post });
					
					key.asString.post;
					":".post;
					
					this.postTree(value, tabs + 1)
				}
			});

		} {
			obj.postTree(tabs);
		};
	}
	
}