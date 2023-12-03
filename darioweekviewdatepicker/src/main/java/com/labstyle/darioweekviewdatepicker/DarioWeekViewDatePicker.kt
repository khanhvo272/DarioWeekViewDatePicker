package com.labstyle.darioweekviewdatepicker

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.util.*

class DarioWeekViewDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
): ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    companion object {
        private fun isSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = Calendar.getInstance()
            cal1.timeInMillis = date1.time
            val cal2 = Calendar.getInstance()
            cal2.timeInMillis = date2.time
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
        }

        private fun isToday(date: Date) = isSameDay(date, Date(System.currentTimeMillis()))

        private fun getFirstDayOfWeekFrom(date: Date): Date {
            val cal = Calendar.getInstance()
            cal.timeInMillis = date.time

            var dow = cal.get(Calendar.DAY_OF_WEEK)
            while (dow > 1) {
                cal.add(Calendar.DATE, -1)
                dow = cal.get(Calendar.DAY_OF_WEEK)
            }
            cal.add(Calendar.DATE, 1)
            return Date(cal.timeInMillis)
        }
    }

    // views
    private val weekdayTextViews = arrayListOf<TextView>()
    private val weekdayNumberViews = arrayListOf<TextView>()
    private val blocks = arrayListOf<ConstraintLayout>()
    private val dots = arrayListOf<ImageView>()
    private val weekDates = arrayListOf<Date>()
    private val selectedDayText: TextView
    private val leftArrow: AppCompatImageButton
    private val rightArrow: AppCompatImageButton
    private val todayText: TextView
    private val lnTodayLayout: LinearLayout

    var shortWeekdays = DateFormatSymbols(Locale.getDefault()).shortWeekdays
    var selectedDate = Date(System.currentTimeMillis())
        private set
    var onSelectionChanged: (Date) -> Unit = {}
    var todayDateString = context.getString(R.string.today)

    init {
        val locale = Locale("vi", "VN")
        Locale.setDefault(locale)

        selectedDate = Date(getToday())
        shortWeekdays =  CustomDateFormatSymbols(locale).shortWeekdays
        inflate(context, R.layout.weekview_datepicker, this)

        // refs to views
        leftArrow = findViewById(R.id.arrowLeft)
        rightArrow = findViewById(R.id.arrowRight)
        selectedDayText = findViewById(R.id.selectedDayText)
        lnTodayLayout = findViewById(R.id.lnTodayLayout)
        todayText = findViewById(R.id.textViewToday)
        weekdayTextViews.addAll(listOf(
            findViewById(R.id.weekdayText1),
            findViewById(R.id.weekdayText2),
            findViewById(R.id.weekdayText3),
            findViewById(R.id.weekdayText4),
            findViewById(R.id.weekdayText5),
            findViewById(R.id.weekdayText6),
            findViewById(R.id.weekdayText7)
        ))
        weekdayNumberViews.addAll(listOf(
            findViewById(R.id.weekdayNumber1),
            findViewById(R.id.weekdayNumber2),
            findViewById(R.id.weekdayNumber3),
            findViewById(R.id.weekdayNumber4),
            findViewById(R.id.weekdayNumber5),
            findViewById(R.id.weekdayNumber6),
            findViewById(R.id.weekdayNumber7)
        ))
        blocks.addAll(listOf(
            findViewById(R.id.block1),
            findViewById(R.id.block2),
            findViewById(R.id.block3),
            findViewById(R.id.block4),
            findViewById(R.id.block5),
            findViewById(R.id.block6),
            findViewById(R.id.block7)
        ))
        dots.addAll(listOf(
            findViewById(R.id.todayDot1),
            findViewById(R.id.todayDot2),
            findViewById(R.id.todayDot3),
            findViewById(R.id.todayDot4),
            findViewById(R.id.todayDot5),
            findViewById(R.id.todayDot6),
            findViewById(R.id.todayDot7)
        ))

        // set fixed weekdays texts
        weekdayTextViews.forEachIndexed { index, textView ->
            textView.text = shortWeekdays[index + 1]
        }

        // handle blocks click
        blocks.forEachIndexed { index, block ->
            block.setOnClickListener {
                setSelection(weekDates[index])
            }
        }

        setTodayButtonText()


        // handle clicks
        rightArrow.setOnClickListener { addDays(7) }
        leftArrow.setOnClickListener { addDays(-7) }
        todayText.setOnClickListener { setToday() }

        setSelection(selectedDate)
    }

    fun setTodayResource(text:String){
        todayDateString = text
        setSelection(selectedDate)
        setTodayButtonText()
    }

    private fun getToday() : Long {
        var calendar = Calendar.getInstance()
        return calendar.timeInMillis;
    }

    var firstInit = false
    private fun setSelection(date: Date) {
        selectedDate = date

        // update long text for selected day
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

        if (isToday(date)) {
            lnTodayLayout.visibility = View.GONE
            selectedDayText.text = dateFormat.format(selectedDate)
        }
        else {
            lnTodayLayout.visibility = View.VISIBLE
            selectedDayText.text = dateFormat.format(selectedDate)
        }

        // index of selected day of week
        val cal = Calendar.getInstance()
        cal.timeInMillis = date.time
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val selectionIndex = dow - 2

        // update selected block
        blocks.forEachIndexed { index, block ->
            block.setBackgroundResource(
                if (isSelectedIndex(index, selectionIndex)) R.drawable.selected_day_bg
                else 0
            )
        }

        // update text styles
        weekdayTextViews.forEachIndexed { index, textView ->
            textView.setTextAppearance(
                if (isSelectedIndex(index, selectionIndex)) R.style.WeekDayTopTextSelected
                else R.style.WeekDayTopText
            )
        }
        weekdayNumberViews.forEachIndexed { index, textView ->
            textView.setTextAppearance(
                if (isSelectedIndex(index, selectionIndex)) R.style.WeekDayTopNumberSelected
                else R.style.WeekDayTopNumber
            )
        }

        updateWeekdayNumbers()

        // update dots
        dots.forEachIndexed { index, imageView ->
            if(isToday(weekDates[index])) {
                imageView.visibility = View.VISIBLE
                if(isSelectedIndex(index, selectionIndex)) {
                    imageView.setImageResource(R.drawable.ic_up_arrow)
                } else {
                    imageView.setImageResource(R.drawable.ic_select_weekday_dot)
                }
            } else {
                imageView.visibility = View.GONE
            }
        }

        onSelectionChanged(selectedDate)
    }

    private fun isSelectedIndex(index: Int, selectionIndex: Int) : Boolean {
        return index == selectionIndex || (selectionIndex == -1 && index == 6)
    }

    private fun setTodayButtonText() {
        val mSpannableString = SpannableString(todayDateString)
        mSpannableString.setSpan(UnderlineSpan(), 0, mSpannableString.length, 0)
        todayText.text = mSpannableString
    }

    private fun updateWeekdayNumbers() {
        // populate the selected day week's dates
        var ccc = Calendar.getInstance()
        ccc.timeInMillis = selectedDate.time
        var weekDateAsDayOfMonth = weekDates.map {
            var cccc = Calendar.getInstance()
            cccc.timeInMillis = it.time
            cccc.get(Calendar.DAY_OF_MONTH)
        }

        if(!weekDateAsDayOfMonth.contains(ccc.get(Calendar.DAY_OF_MONTH))) {
            weekDates.clear()
            val date = getFirstDayOfWeekFrom(selectedDate)
            val cal = Calendar.getInstance()

            cal.timeInMillis = date.time

            weekDates.add(Date(cal.timeInMillis))
            var i = 1
            while (i < 7) {
                cal.add(Calendar.DATE, 1)
                weekDates.add(cal.time)
                i++
            }
            // update the views
            weekdayNumberViews.forEachIndexed { index, textView ->
                cal.timeInMillis = weekDates[index].time
                textView.text = "${cal.get(Calendar.DAY_OF_MONTH)}"
            }
        }
    }

    private fun addDays(days: Int) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDate.time
        cal.add(Calendar.DATE, days)
        setSelection(cal.time)
    }

    private fun setToday() = setSelection(Date(getToday()))
}