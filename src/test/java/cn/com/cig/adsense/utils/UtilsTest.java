package cn.com.cig.adsense.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class UtilsTest {

	public static void main(String[] args) {

		List<Integer> test = new ArrayList<>();
		test.add(1);
		test.add(null);
		test.add(2);
		
		System.out.println(test);
		test = ImmutableList.copyOf(Iterables.filter(test, Predicates.notNull()));
		System.out.println(test);
		
		String matchedCompetitiveModelsStr = test.stream().filter((tag) -> tag != null)
				.distinct().limit(Constant.MATCHED_TAGS_NUM_LIMIT)
				.map((tag) -> Integer.toString(tag))
				.collect(Collectors.joining(","));
		System.out.println(matchedCompetitiveModelsStr);
		
		matchedCompetitiveModelsStr = test.stream().filter(Objects::nonNull).distinct()
				.limit(Constant.MATCHED_TAGS_NUM_LIMIT)
				.map((tag) -> Integer.toString(tag))
				.collect(Collectors.joining(","));
		System.out.println(matchedCompetitiveModelsStr);
	}

}
