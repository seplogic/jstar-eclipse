package basic;

import com.jstar.eclipse.annotations.Import;
import com.jstar.eclipse.annotations.Predicates;
import com.jstar.eclipse.annotations.Predicate;
import com.jstar.eclipse.annotations.Spec;
import com.jstar.eclipse.annotations.Specs;

@Import("basic.Cell")

@Predicates({
	@Predicate(predicate = "Val(x,content=y)", formula = "y!=y"),
	@Predicate(predicate = "DVal(x,content=y)", formula = "Val$basic.Cell(x,{content=builtin_plus(y,y)})")
})


class DCell extends Cell {
	
	@Spec(pre = "", post = "DVal$(@this:, {content = _w})")
	DCell() {
		super.set(super.get() + super.get());
	}

	@Specs({
		@Spec(pre = "Val$(this, {content=_X})", post = "Val$(this, {content=y})"),
		@Spec(pre = "DVal$(@this:, {content=_X})", post = "DVal$(@this:, {content=y})")
	})
	void set(int y) {
		super.set(y + y);
	}

	@Specs({
		@Spec(pre = "Val$(this, {content=_X})", post = "_X=return * Val$(this, {content=_X})"),
		@Spec(pre = "DVal$(this, {content=_X})", post = "builtin_plus(_X,_X)=return * DVal$(this, {content=_X})")
	})
	int get() {
		return super.get();
	}

}
