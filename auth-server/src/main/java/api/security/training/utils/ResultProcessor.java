package api.security.training.utils;

import java.util.function.Consumer;

import com.spencerwi.either.Result;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResultProcessor {

	public <R> void processResult(Result<R> result, Consumer<R> resultConsumer) throws Exception {
		result.ifOk(resultConsumer);
		if (result.isErr()) {
			throw result.getException();
		}
	}

}
