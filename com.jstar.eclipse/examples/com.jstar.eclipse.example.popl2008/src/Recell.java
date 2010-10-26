import com.jstar.eclipse.annotations.Import;
import com.jstar.eclipse.annotations.InitSpec;
import com.jstar.eclipse.annotations.Predicate;
import com.jstar.eclipse.annotations.Spec;

@Import("Cell")

@Predicate(predicate = "Val(x,content=y;oldcon=z)", formula = "Val$Cell(x, {content=y}) * field(x, <Recell: int bak>, z)")

@InitSpec(pre = "", post = "Val$(this, {content=_x; oldcon=_z})")

class Recell extends Cell {
	int bak;

	@Spec(pre = "Val$(@this:, {content=_X; oldcon=_})", post = " Val$(@this:, {content=x; oldcon=_X})")
	void set(int x) {
		bak = super.get();
		super.set(x);
	}

	@Spec(pre = "Val$(this, {content=_X; oldcon=_Y})", post = "_X=return * Val$(this, {content=_X; oldcon=_Y})")
	int get() {
		return super.get();
	}

}
