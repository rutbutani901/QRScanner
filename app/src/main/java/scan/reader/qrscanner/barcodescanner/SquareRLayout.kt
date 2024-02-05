package scan.reader.qrscanner.barcodescanner

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout


class SquareRLayout: RelativeLayout {
    constructor(context: Context?, attributes: AttributeSet?):super(context,attributes){}
    constructor(context: Context?):super(context){}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthMeasureSpecMode= MeasureSpec.getMode(widthMeasureSpec)
        val heightMeasureSpecMode= MeasureSpec.getMode(heightMeasureSpec)
        val widthMeasureSpecSize= MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasureSpecSize= MeasureSpec.getSize(heightMeasureSpec)

        val newSize=if(widthMeasureSpecMode== MeasureSpec.EXACTLY && widthMeasureSpecSize>0) {
            widthMeasureSpecSize
        }
        else if(heightMeasureSpecMode== MeasureSpec.EXACTLY && heightMeasureSpecSize>0) {
            heightMeasureSpecSize
        }
        else{
            if(widthMeasureSpecSize<heightMeasureSpecSize) widthMeasureSpecSize else heightMeasureSpecSize
        }
        val finalMeasureSpec= MeasureSpec.makeMeasureSpec(newSize, MeasureSpec.EXACTLY)
        super.onMeasure(finalMeasureSpec, finalMeasureSpec)
    }
}