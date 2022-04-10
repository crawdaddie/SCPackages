// eq[ \uvw ].drawFunc = { |vw|
// 
// 
// 			var freqs, svals, values, bounds, zeroline;
// 			var freq = 1200, rq = 0.5, db = 12;
// 			var min = 20, max = 22050, range = 24;
// 			var vlines = [100,1000,10000];
// 			var dimvlines = [25,50,75, 250,500,750, 2500,5000,7500];
// 			var hlines = [-18,-12,-6,6,12,18];
// 			var pt, strOffset = 11;
// 			
// 			if( GUI.id === 'swing' ) { strOffset = 14 };
// 			
// 			bounds = vw.bounds.moveTo(0,0);
// 			
// 			#freq,db,rq = eq[ \frdb ][0] ? [ freq, db, rq ];
// 			
// 			freqs = ({|i| i } ! (bounds.width+1));
// 			freqs = freqs.linexp(0, bounds.width, min, max );
// 			
// 			values = [
// 				BLowShelf.magResponse( freqs, 44100, eq[ \frdb ][0][0], eq[ \frdb ][0][2], 
// 					eq[ \frdb ][0][1]),
// 				BPeakEQ.magResponse( freqs, 44100, eq[ \frdb ][1][0], eq[ \frdb ][1][2], 
// 					eq[ \frdb ][1][1]),
// 				BPeakEQ.magResponse( freqs, 44100, eq[ \frdb ][2][0], eq[ \frdb ][2][2], 
// 					eq[ \frdb ][2][1]),
// 				BPeakEQ.magResponse( freqs, 44100, eq[ \frdb ][3][0], eq[ \frdb ][3][2], 
// 					eq[ \frdb ][3][1]),
// 				BHiShelf.magResponse( freqs, 44100, eq[ \frdb ][4][0], eq[ \frdb ][4][2], 
// 					eq[ \frdb ][4][1])
// 					].ampdb.max(-200).min(200);
// 			
// 			zeroline = 0.linlin(range.neg,range, bounds.height, 0, \none);
// 			
// 			svals = values.sum.linlin(range.neg,range, bounds.height, 0, \none);
// 			values = values.linlin(range.neg,range, bounds.height, 0, \none);
// 			
// 			vlines = vlines.explin( min, max, 0, bounds.width );
// 			dimvlines = dimvlines.explin( min, max, 0, bounds.width );
// 			
// 			pt = eq[ \frdb ].collect({ |array|
// 				(array[0].explin( min, max, 0, bounds.width ))
// 				@
// 				(array[1].linlin(range.neg,range,bounds.height,0,\none));
// 				});
// 
// 				Pen.color_( Color.white.alpha_(0.25) );
// 				Pen.roundedRect( bounds, [6,6,0,0] ).fill;
// 				
// 				Pen.color = Color.gray(0.2).alpha_(0.5);
// 				//Pen.strokeRect( bounds.insetBy(-1,-1) );
// 				
// 				//Pen.addRect( bounds ).clip;
// 				Pen.roundedRect( bounds.insetBy(0,0), [6,6,0,0] ).clip;
// 				
// 				Pen.color = Color.gray(0.2).alpha_(0.125);
// 				
// 				hlines.do({ |hline,i|
// 					hline = hline.linlin( range.neg,range, bounds.height, 0, \none );
// 					Pen.line( 0@hline, bounds.width@hline )
// 					});
// 				dimvlines.do({ |vline,i|
// 					Pen.line( vline@0, vline@bounds.height );
// 					});
// 				Pen.stroke;
// 			
// 				Pen.color = Color.gray(0.2).alpha_(0.5);
// 				vlines.do({ |vline,i|
// 					Pen.line( vline@0, vline@bounds.height );
// 					});
// 				Pen.line( 0@zeroline, bounds.width@zeroline ).stroke;
// 				
// 				/*
// 				Pen.color = Color.white.alpha_(0.5);
// 				Pen.fillRect( Rect( 33, 0, 206, 14 ) );
// 				*/
// 				
// 				Pen.font = eq[ \font ];
// 				
// 				Pen.color = Color.gray(0.2).alpha_(0.5);
// 				hlines.do({ |hline|
// 					Pen.stringAtPoint( hline.asString ++ "dB", 
// 						3@(hline.linlin( range.neg,range, bounds.height, 0, \none ) 
// 							- strOffset) );
// 					});
// 				vlines.do({ |vline,i|
// 					Pen.stringAtPoint( ["100Hz", "1KHz", "10KHz"][i], 
// 						(vline+2)@(bounds.height - (strOffset + 1)) );
// 					});
// 				
// 				//Pen.roundedRect( bounds.insetBy(0.5,0.5), [5,5,0,0] ).stroke;
// 				
// 				/*
// 				if( eq[ \selected ] != -1 )
// 					{ Pen.stringAtPoint(
// 						[ "low shelf: %hz, %dB, rs=%",
// 						  "peak 1: %hz, %dB, rq=%",
// 						  "peak 2: %hz, %dB, rq=%",
// 						  "peak 3: %hz, %dB, rq=%",
// 						  "hi shelf: %hz, %dB, rs=%"
// 						][ eq[ \selected ] ].format(
// 							eq[ \frdb ][eq[ \selected ]][0],
// 							eq[ \frdb ][eq[ \selected ]][1],
// 							eq[ \frdb ][eq[ \selected ]][2]
// 							),
// 						35@1 );
// 					 }
// 					 { Pen.stringAtPoint( "shift: snap, alt: rq", 35@1 ); };
// 				*/
// 						
// 				values.do({ |svals,i|
// 					var color;
// 					color = Color.hsv(
// 						i.linlin(0,values.size,0,1), 
// 						0.75, 0.5).alpha_(if( eq[ \selected ] == i ) { 0.75 } { 0.25 });
// 					Pen.color = color;
// 					Pen.moveTo( 0@(svals[0]) );
// 					svals[1..].do({ |val, i|
// 						Pen.lineTo( (i+1)@val );
// 						});
// 					Pen.lineTo( bounds.width@(bounds.height/2) );
// 					Pen.lineTo( 0@(bounds.height/2) );
// 					Pen.lineTo( 0@(svals[0]) );
// 					Pen.fill;
// 					
// 					Pen.addArc( pt[i], 5, 0, 2pi );
// 					
// 					Pen.color = color.alpha_(0.75);
// 					Pen.stroke;
// 		
// 					});
// 				
// 				Pen.color = Color.blue(0.5);
// 				Pen.moveTo( 0@(svals[0]) );
// 				svals[1..].do({ |val, i|
// 					Pen.lineTo( (i+1)@val );
// 					});
// 				Pen.stroke;
// 				
// 				Pen.extrudedRect( bounds, [6,6,0,0], 1, inverse: true );
// 				
// 				
// 		
// 			
// 			};
~edit = M { arg node, controls;
   var eqGUIControls = EQuiParams(*controls);
   EQui(nil, nil, eqGUIControls).front;
};

