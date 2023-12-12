package br.com.erudio.integrationtests.vo.wrappers;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.erudio.integrationtests.vo.BookVO;

public class BookEmbaddedVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("bookVOList")
	private List<BookVO> books;

	public BookEmbaddedVO() {
	}

	public List<BookVO> getBooks() {
		return books;
	}

	public void setBooks(List<BookVO> books) {
		this.books = books;
	}

	@Override
	public int hashCode() {
		return Objects.hash(books);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookEmbaddedVO other = (BookEmbaddedVO) obj;
		return Objects.equals(books, other.books);
	}

}
