+ Rect {
	not { | otherRect |
		if ((otherRect.top == this.top) && (otherRect.bottom == this.bottom), {
			if (this.intersects(otherRect).not, {
				^this
			});

			if ((otherRect.left > this.left) && (otherRect.right < this.right), {
				^[
					Rect.newSides(this.left, this.top, otherRect.left, this.bottom),
					Rect.newSides(otherRect.right, this.top, this.right, this.bottom)
				]	
			});
			
			if ((otherRect.left > this.left) && (otherRect.right >= this.right), {
				^Rect.newSides(this.left, this.top, otherRect.left, this.bottom)
			});
			
			if ((otherRect.left <= this.left) && (otherRect.right < this.right), {
				^Rect.newSides(otherRect.right, this.top, this.right, this.bottom)
			});
		
		});
		^this
	}
}