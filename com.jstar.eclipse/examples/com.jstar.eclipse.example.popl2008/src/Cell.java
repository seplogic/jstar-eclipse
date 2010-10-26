import com.jstar.eclipse.annotations.Import;
import com.jstar.eclipse.annotations.InitSpec;
import com.jstar.eclipse.annotations.Predicate;
import com.jstar.eclipse.annotations.Spec;

@Import("java.lang.Object")

@Predicate(predicate = "Val(x,content=y)", formula = "x.<Cell: int val> |-> y")

@InitSpec(pre = "", post = "Val$(this,{content=_ })")

class Cell {
	int val;

	@Spec(pre = "Val$(this, {content=_})", post = "Val$(this, {content=x})")
	void set(int x) {
		val = x;
	}

	@Spec(pre = "Val$(this, {content=_X})", post = "_X=return * Val$(@this:, {content=_X})")
	int get() {
		return val;
	}
}