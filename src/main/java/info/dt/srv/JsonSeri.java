package info.dt.srv;

import flexjson.JSONSerializer;
import flexjson.transformer.MapTransformer;
import info.dt.data.TimeSheet;
import info.dt.data.TimeSheetPosition.Status;
import info.dt.report.IReportPosition;
import info.dt.report.IReportView;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableInterval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

@Slf4j
public class JsonSeri implements IJsonSerializer {

	@Inject
	private IReportView reportView;

	public String toJson(TimeSheet timeSheet, Set<String> idsOnClient,
			ReadableInterval currentInterval) {
		List<IReportPosition> positions = reportView.toReportPositions(timeSheet);

		Set<String> hashes = Sets.newHashSet();

		for (IReportPosition pos : positions) {
			String hash = hash(pos, timeSheet);
			hashes.add(hash);
			if (pos.getPathes().size() > 1) {
				for (Entry<List<String>, Duration> entry : pos.getPathes().entrySet()) {
					String subhash = hash(entry.getKey(), entry.getValue(), pos);
					hashes.add(subhash);
				}
			}
		}

		if (hashes.equals(idsOnClient)) {
			return "";
		}

		JSONSerializer seri = new JSONSerializer();
		seri.transform(new Aasf(timeSheet), IReportPosition.class);
		Map<String, Object> result = Maps.newHashMap();
		result.put("positions", positions);
		result.put("sum", timeSum(positions));
		result.put("now", newDateTimeSortFormatter().print(DateTime.now()));
		result.put("start", newDateTimeFormatter()
				.print(currentInterval.getStart()));
		result.put("end", newDateTimeFormatter().print(currentInterval.getEnd()));
		return seri.deepSerialize(result);
	}

	private String timeSum(List<IReportPosition> positions) {
		Duration sum = Duration.ZERO;
		for (IReportPosition position : positions) {
			sum = sum.plus(position.getDuration());
		}
		return newPeriodFormatter().print(sum.toPeriod());
	}

	private static DateTimeFormatter newDateTimeSortFormatter() {
		return new DateTimeFormatterBuilder() //
				.appendHourOfDay(2).appendLiteral(":")//
				.appendMinuteOfHour(2) //
				.appendLiteral(":") //
				.appendSecondOfMinute(2) //
				.appendLiteral(":") //
				.appendMillisOfSecond(2).toFormatter();
	}

	private static DateTimeFormatter newDateTimeFormatter() {
		return new DateTimeFormatterBuilder() //
				.appendYear(4, 4) //
				.appendLiteral("-")//
				.appendMonthOfYear(2) //
				.appendLiteral("-") //
				.appendDayOfMonth(2) //
				.toFormatter();
	}

	private static PeriodFormatter newPeriodFormatter() {
		PeriodFormatter formatter = new PeriodFormatterBuilder() //
				.appendHours() //
				.appendSuffix("h") //
				.appendSeparator(" ") //
				.appendMinutes() //
				.appendSuffix("m").toFormatter();
		return formatter;
	}

	private static String hash(Object... objects) {
		StringBuilder sb = new StringBuilder();
		for (Object o : objects) {
			if (log.isDebugEnabled()) {
				Method[] declaredMethods = o.getClass().getDeclaredMethods();
				boolean hasHash = false;
				for (Method method : declaredMethods) {
					if (method.getName().startsWith("hash")) {
						hasHash = true;

					}
				}
				if (!hasHash) {
					Method[] methods = o.getClass().getMethods();
					for (Method method : methods) {
						if (method.getName().startsWith("hash")) {
							Class<?> declaringClass = method.getDeclaringClass();
							if (Object.class.getCanonicalName().equals(
									declaringClass.getCanonicalName())) {
								log.warn("{} have to implement hashCode", o.getClass()
										.getCanonicalName());
								break;
							}
						}
					}
				}
			}
			String string = o.hashCode() + "";
			if (false && log.isWarnEnabled()) {
				if (string.contains("@")) {
					log.warn("@ in " + o.getClass().getCanonicalName() + string);
				}
			}
			sb.append(string);
		}
		return Hashing.sha1().hashString(sb.toString()).toString();
	}

	private static class Aasf extends MapTransformer {

		private final TimeSheet timeSheet;

		public Aasf(TimeSheet timeSheet) {
			this.timeSheet = timeSheet;
		}

		@Override
		public void transform(Object object) {
			IReportPosition timeSheetPosition = (IReportPosition) object;
			Map<String, Object> map = Maps.newHashMap();
			map.put("htmlid", hash(timeSheetPosition, timeSheet));
			String id = timeSheetPosition.getId();
			Status status = timeSheetPosition.getStatus();
			if (status != Status.NONE) {
				map.put("status", cssClass(status));
				map.put("statusName", status.name());
			}
			map.put("id", id);
			map.put("path", formatPath(timeSheetPosition.getPath(), id));
			map.put("title", timeSheetPosition.getTitle());
			map.put("comment", timeSheetPosition.getCommentLines());
			map.put("duration", toDuration(timeSheetPosition.getDuration()));
			map.put("durationPercentage", timeSheetPosition.getDurationPercentage());
			List<Map<String, Object>> list = Lists.newArrayList();
			for (Entry<List<String>, Duration> entry : timeSheetPosition.getPathes()
					.entrySet()) {
				Map<String, Object> submap = Maps.newHashMap();
				submap.put("htmlid",
						hash(entry.getKey(), entry.getValue(), timeSheetPosition));
				submap.put("duration", toDuration(entry.getValue()));
				long durationMillis = timeSheetPosition.getDuration().getMillis();
				if (durationMillis <= 0) {
					throw new IllegalStateException("check "
							+ timeSheetPosition.getPath());
				}
				long entryMillis = entry.getValue().getMillis();
				int subPercentage = (int) (100 * entryMillis / durationMillis);
				submap.put("durationPercentage", subPercentage + "");
				submap.put("path", formatPath(entry.getKey(), id));

				list.add(submap);
			}
			map.put("sub", list);
			super.transform(map);

		}

		private String cssClass(Status status) {
			switch (status) {
			case TODO:
				return "label-warning";
			case FIXME:
				return "label-important";

			default:
				return "";
			}
		}

		public List<String> formatPath(Iterable<String> path, String id) {

			List<String> list = Lists.newArrayList(path);
			list.remove(id);
			return list;

		}

		private String toDuration(Duration value) {
			return newPeriodFormatter().print(value.toPeriod());
		}

	}

}
